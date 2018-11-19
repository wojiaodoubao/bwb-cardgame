package com.bl.cardgame;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class DrawCardAction extends CardAction {
    public DrawCardAction() {
        this(-1);// action comes from game
    }

    public DrawCardAction(int srcPlayerIndex) {
        super(TYPE.NON_EFFECT, srcPlayerIndex);
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = super.toJson();
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        super.fromJson(jobj);
    }
}
