package com.bl.cardgame;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class PlayCardAction extends CardAction {
    int cardIndex;
    int source;
    int[] targets;
    public PlayCardAction() {}
    public PlayCardAction(TYPE type, int srcPlayerIndex, int cardIndex, int source, int[] targets) {
        super(type, srcPlayerIndex);
        this.cardIndex = cardIndex;
        this.source = source;
        this.targets = targets;
    }
    public int getCardIndex() {
        return cardIndex;
    }
    public int getSource() {
        return source;
    }
    public int[] getTargets() {
        return targets;
    }
    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = super.toJson();
        jobj.put(PlayCardAction.class.getName() + "cardIndex", cardIndex);
        jobj.put(PlayCardAction.class.getName() + "source", source);
        JSONArray array = new JSONArray();
        for (int i=0;i<array.length();i++) {
            array.put(targets[i]);
        }
        jobj.put(PlayCardAction.class.getName() + "targets", array);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        super.fromJson(jobj);
        cardIndex = (int) jobj.get(PlayCardAction.class.getName() + "cardIndex");
        source = (int) jobj.get(PlayCardAction.class.getName() + "source");
        JSONArray array =
            (JSONArray) jobj.get(PlayCardAction.class.getName() + "targets");
        targets = new int[array.length()];
        for (int i=0;i<array.length();i++) {
            targets[i] = array.getInt(i);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() + cardIndex + source
            + (targets == null?0:targets.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof PlayCardAction) {
            PlayCardAction pca = (PlayCardAction) obj;

            return super.equals(pca) && pca.cardIndex == cardIndex
                && pca.source == source && Arrays.equals(pca.targets, targets);
        }
        return false;
    }
}
