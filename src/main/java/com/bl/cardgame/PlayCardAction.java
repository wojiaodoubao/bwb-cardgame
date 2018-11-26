package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayCardAction extends CardAction {
    int cardIndex;
    List<Integer> targets = new ArrayList<>();
    public PlayCardAction() {}
    public PlayCardAction(TYPE type, int srcPlayerIndex, int cardIndex, List<Integer> targets) {
        super(type, srcPlayerIndex);
        this.cardIndex = cardIndex;
        this.targets = targets;
    }
    public int getCardIndex() {
        return cardIndex;
    }
    public List<Integer> getTargets() {
        return targets;
    }
    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = super.toJson();
        jobj.put(PlayCardAction.class.getName() + "cardIndex", cardIndex);
        JSONArray array = JsonWrapper.collectionToJsonArray(targets, new JSONArray());
        jobj.put(PlayCardAction.class.getName() + "targets", array);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        super.fromJson(jobj);
        cardIndex = (int) jobj.get(PlayCardAction.class.getName() + "cardIndex");
        JSONArray array =
            (JSONArray) jobj.get(PlayCardAction.class.getName() + "targets");
        targets =
            (ArrayList<Integer>) JsonWrapper.jsonArrayToCollection(array, new ArrayList());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (targets == null?0:targets.size())
            + cardIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof PlayCardAction) {
            PlayCardAction pca = (PlayCardAction) obj;
            boolean same = true;
            if (pca.targets == null) {
                same = targets == null;
            } else {
                if (targets == null) {
                    same = false;
                } else if (pca.targets.size() != targets.size()) {
                    same = false;
                } else {
                    for (int i=0;i<pca.targets.size();i++) {
                        if (pca.targets.get(i) != targets.get(i)) {
                            same = false;
                            break;
                        }
                    }
                }
            }
            return same && super.equals(pca) && pca.cardIndex == cardIndex;
        }
        return false;
    }
}
