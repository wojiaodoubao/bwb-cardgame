package com.bl.rpc;

import java.util.concurrent.atomic.AtomicInteger;

public class Packet {
    long id;
    private byte[] data;
    private byte[] res;
    private boolean exception = false;
    public Packet() {}
    @Override
    public int hashCode() {
        return (int)id;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Packet) {
            return id == ((Packet) obj).id;
        }
        return false;
    }
    private static AtomicInteger packetId = new AtomicInteger(0);
    public static Packet createPacket(byte[] data) {
        Packet packet = new Packet();
        packet.data = new byte[data.length];
        for (int i=0;i<data.length;i++) {
            packet.data[i] = data[i];
        }
        packet.id = getNextPacketId();
        return packet;
    }
    public static int getNextPacketId() {
        return packetId.incrementAndGet();
    }
    public long getId() {
        return this.id;
    }
    public byte[] getData() {
        return this.data;
    }
    public byte[] getRes() {
        return this.res;
    }
    public boolean isAnyException() {
        return this.exception;
    }
    public Packet setData(byte[] data) {
        this.data = data;
        return this;
    }
    public Packet setRes(byte[] res) {
        this.res = res;
        return this;
    }
    public Packet setException(boolean exception) {
        this.exception = exception;
        return this;
    }
    public Packet setId(long id) {
        this.id = id;
        return this;
    }
}
