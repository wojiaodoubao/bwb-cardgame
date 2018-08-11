package com.bl.rpc;

import com.bl.Action;
import com.bl.ClientGameProtocol;
import com.bl.Game;
import com.bl.cardgame.Card;
import com.bl.cardgame.CardAction;
import com.bl.cardgame.CardGame;
import com.bl.serialization.Serializer;
import org.codehaus.jettison.json.JSONException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RPCServer extends Thread {
    // TODO:队列handlers
    class Handler implements Runnable {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        public Handler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }
        @Override
        public void run() {
            Packet packet = null;
            while (true) {
                try {
                    packet = Connection.receivePacket(in);
                    Action action = Serializer.byteToaction(packet.getData());
                    Game res = game.doAction(action);
                    byte[] resData = Serializer.gameTobyte(res);
                    packet.setException(false).setRes(resData);
                } catch (Exception e) {
                    // TODO:LOG stack
                    e.printStackTrace();
                    if (packet != null) {
                        String msg = e.getMessage();
                        packet.setException(true).setRes(msg.getBytes());
                    }
                } finally {
                    if (packet != null) {
                        try {
                            Connection.sendPacket(packet, out);
                            System.out.println("rpc server:send packet");
                        } catch (IOException e) {
                            // If connection lost (may be closed by client due to timeout),
                            // we will fail in sending back response.
                            // We already did the action to game, so client need to be well
                            // designed when doing retry.
                            // TODO:LOG stack
                            e.printStackTrace();
                        }
                    }
                    if (socket.isClosed()) {
                        break;
                    }
                }
            }
        }
    }
    ClientGameProtocol game;
    int port;
    boolean close;
    ServerSocket serverSocket;
    ArrayList<Handler> handlers = new ArrayList<Handler>();
    public RPCServer(ClientGameProtocol game, int port) {
        this.game = game;
        this.port = port;
        this.close = false;
    }
    public void close() throws IOException {
        this.close = true;
        if (serverSocket != null) {
            serverSocket.close();
        }
        serverSocket = null;
        handlers.clear();
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!close) {
                Socket socket = serverSocket.accept();
                new Thread(new Handler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
