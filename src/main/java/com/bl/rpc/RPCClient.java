package com.bl.rpc;

import com.bl.Action;
import com.bl.ClientGameProtocol;
import com.bl.Game;
import com.bl.Player;
import com.bl.cardgame.Card;
import com.bl.cardgame.CardGame;
import com.bl.cardgame.GetCardGameAction;
import com.bl.serialization.Serializer;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;

public class RPCClient implements ClientGameProtocol {
    private String host;
    private int port;
    private Connection connection;
    public RPCClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.connection = new Connection(host, port);
    }

    public synchronized Game doAction(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException {
        byte[] data = null;
        try {
            data = Serializer.actionTobyte(action);
        } catch (JSONException e) {
            // LOG
            throw new IOException("fail in serialize action", e);
        }
        Packet packet = Packet.createPacket(data);
        int retries = 0;
        while (true) {
            connection.sendPacket(packet);
            synchronized (packet) {
                while (packet.getRes() == null) {
                    try {
                        packet.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (packet.isAnyException()) {
                String msg = new String(packet.getRes());
                if (msg.contains("Read timed out") && retries < 0) {
                    retries++;
                    packet.setId(Packet.getNextPacketId());
                    continue;
                }
                throw new IOException("Got server exception:"+msg);
            }
            try {
                return Serializer.byteToGame(packet.getRes());
            } catch (JSONException e) {
                throw new IOException("fail in deserialize response", e);
            }
        }
    }

    public synchronized Game getGame(Player player) throws Card.SanityException, Action.UnknownActionException, CardGame.GameException, IOException {
        return doAction(new GetCardGameAction());
    }

    public void close() throws IOException {
        connection.close();
    }
}
