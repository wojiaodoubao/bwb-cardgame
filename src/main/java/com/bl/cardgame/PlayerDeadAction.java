package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

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
}
