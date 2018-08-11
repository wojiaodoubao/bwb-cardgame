package com.bl.rpc;

import com.bl.Game;
import com.bl.serialization.Serializer;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Connection extends Thread {
    String host;
    int port;
    Socket socket;
    boolean close;
    DataInputStream in;
    DataOutputStream out;
    // TODO:小心leak memory
    HashMap<Long,Packet> results = new HashMap<Long,Packet>();
    public Connection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        close = false;
        this.start();
    }
    synchronized private void connectTo() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(10000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }
    synchronized private void disconnectTo() throws IOException {
        in.close();
        out.close();
        socket.close();
        in = null;
        out = null;
        socket = null;
    }
    synchronized public void sendPacket(Packet packet) throws IOException {
        if (close) {
            throw new RuntimeException("client has been closed !");
        }
        while (socket == null || socket.isClosed()) {
            // TODO:add max retry times
            connectTo();
        }
        sendPacket(packet, out);
        synchronized (results) {
            results.put(packet.getId(), packet);
            results.notifyAll();
        }
    }
    @Override
    public void run() {
        while (!close) {
            try {
                synchronized (results) {
                    while (results.size() == 0) {
                        results.wait();
                    }
                }
                Packet rcpacket = receivePacket(in);
                Packet packet = null;
                synchronized (results) {
                    packet = results.get(rcpacket.getId());
                    results.remove(packet.getId());
                }
                assert packet != null;
                // 走不到下面client一定重试，重试失败一定close清空results;
                // 反之走到一定results.remove
                packet.setData(rcpacket.getData())
                        .setRes(rcpacket.getRes())
                        .setException(rcpacket.isAnyException());
                synchronized (packet) {
                    packet.notifyAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
//                LOG.warn(e);
                try {
                    synchronized (this) {
                        // synchronized this because connection has lost, we are going to make a
                        // clean close and start a new connection.
                        // a new connection will lost all context, which means the waiting rpcs
                        // won't get responses any more.
                        // all waiting rpc will get an Exception and decide whether to retry.
                        Iterator<Map.Entry<Long, Packet>> iterator = results.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<Long, Packet> entry = iterator.next();
                            Packet packet = entry.getValue();
                            packet.setException(true);
                            packet.setRes(e.getMessage().getBytes());
                            synchronized (packet) {
                                packet.notifyAll();
                            }
                            iterator.remove();
                        }
                        disconnectTo();
                        while (socket == null || socket.isClosed()) {
                            connectTo();
                        }
                    }
                } catch (IOException e1) {
                }
            }
        }
    }
    synchronized public void close() throws IOException {
        close = true;
        in.close();
        out.close();
        socket.close();
        results.clear();
    }
    public static void sendPacket(Packet packet, DataOutputStream out) throws IOException {
        byte[] data = Serializer.packetTobyte(packet);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }
    public static Packet receivePacket(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] buf = new byte[len];
        int left = buf.length;
        while (left > 0) {
            int size = in.read(buf, buf.length-left, left);
            left -= size;
        }
        Packet packet = Serializer.byteTopacket(buf);
        return packet;
    }
}
