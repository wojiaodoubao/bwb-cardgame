package com.bl.cardgame;

import com.bl.Action;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class CardAction extends Action {

    public enum TYPE {
        NON_EFFECT,EFFECT
    }
    public TYPE type = TYPE.NON_EFFECT;
    public long timestamp = System.currentTimeMillis();
    // srcPlayerIndex=-1 means srcPlayer comes from game, not a player. 比如玩家打出牌造成交换牌，那么交换牌action来自game
    public int srcPlayerIndex = -1;// CardAction source is different from like PlayCardAction's src target nor ExchangeCardAction's src taret.

    public CardAction() {
    }

    public CardAction(TYPE type, int srcPlayerIndex) {
        this.type = type;
        this.srcPlayerIndex = srcPlayerIndex;
    }

    public boolean shoudRecord() {// TODO:abstract?
        return true;
    }

    public int getSrcPlayer() {// TODO:delete?
        return this.srcPlayerIndex;
    }

    public TYPE getType() {// TODO:delete?
        return this.type;
    }

    public void set(TYPE type,long timestamp,int srcPlayerIndex) {
        this.type = type;
        this.timestamp = timestamp;
        this.srcPlayerIndex = srcPlayerIndex;
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put("type", type);
        jobj.put("timestamp", timestamp);
        jobj.put("srcPlayerIndex", srcPlayerIndex);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj)
        throws IOException, JSONException {
        this.type = TYPE.valueOf(jobj.getString("type"));
        this.timestamp = jobj.getLong("timestamp");
        this.srcPlayerIndex = jobj.getInt("srcPlayerIndex");
    }
}
