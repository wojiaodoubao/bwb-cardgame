package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class ExchangeCardAction extends CardAction {
    int srcPid;
    int dstPid;

    public ExchangeCardAction() {}

    public ExchangeCardAction(int srcPlayerId, int dstPlayerId) {
        super();
        this.srcPid = srcPlayerId;
        this.dstPid = dstPlayerId;
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
