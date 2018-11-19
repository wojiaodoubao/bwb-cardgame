package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class PlayerDeadAction extends CardAction {
    int playerIndex;
    String msg;

    public PlayerDeadAction(){};

    public PlayerDeadAction(int playerIndex, String msg) {
        super();
        this.playerIndex = playerIndex;
        this.msg = msg;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = super.toJson();
        JsonWrapper.objectToJson(this.getClass(), this, jobj);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        super.fromJson(jobj);
        JsonWrapper.jsonToObject(this.getClass(), this, jobj);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + playerIndex + (msg == null?0:msg.length());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof PlayerDeadAction) {
            PlayerDeadAction pda = (PlayerDeadAction) obj;

            return super.equals(pda) && pda.msg.equals(msg)
                && pda.playerIndex == playerIndex;
        }
        return false;
    }
}
