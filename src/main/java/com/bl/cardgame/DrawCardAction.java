package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class DrawCardAction extends CardAction {
    int source;

    public DrawCardAction() {}

    public DrawCardAction(int source) {
        this(-1, source);// action comes from game
    }

    public DrawCardAction(int srcPlayerIndex, int source) {
        super(TYPE.NON_EFFECT, srcPlayerIndex);
        this.source = source;
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
