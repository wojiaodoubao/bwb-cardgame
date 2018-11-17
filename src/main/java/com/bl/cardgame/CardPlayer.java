package com.bl.cardgame;

import com.bl.Player;
import com.bl.ipc.jason.JsonWrapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardPlayer extends Player {
    List<Card> handCard = new ArrayList<Card>();
    List<Feature> features = new ArrayList<Feature>();
    public CardPlayer() {}
    public CardPlayer(int id, String name) {
        super(id, name);
    }

    public void addCard(Card card) {// add card to hand cards tail
        handCard.add(card);
    }

    public void insertCard(int index, Card card) {// insert card to hand cards
        handCard.add(index, card);
    }

    public Card removeCard(int index) {
        if (index <0 || index>=handCard.size()) {
            return null;
        }
        return handCard.remove(index);
    }

    public Card getCard(int index) {
        if (handCard.size() <= index) {
            return null;
        }
        return handCard.get(index);
    }

    public Feature getFeature(Class<? extends Feature> clazz) {
        for (int i=0;i<features.size();i++) {
            Feature f = features.get(i);
            if (f.getClass() == clazz) {
                return features.get(i);
            }
        }
        return null;
    }

    public Feature removeFeature(Class<? extends Feature> clazz) {
        for (int i=0;i<features.size();i++) {
            Feature f = features.get(i);
            if (f.getClass() == clazz) {
                return features.remove(i);
            }
        }
        return null;
    }

    public void addFeature(Feature feature) {
        for (Feature f : features) {
            if (f.getClass() == feature.getClass()) return;
        }
        features.add(feature);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString()+"\n");
        sb.append("features:\n");
        for (Feature feature:features) {
            sb.append(feature.getClass());
        }
        sb.append("handcards:\n");
        for (int i=0;i<handCard.size();i++) {
            sb.append("["+i+"]"+handCard.get(i).getPoint()+" "+handCard.get(i).getClass());
        }
        return sb.toString();
    }
    public List<Card> getHandCards() {
        return handCard;
    }
    public List<Feature> getFeatures() {
        return features;
    }
    public CardPlayer setHandCards(List<Card> cards) {
        this.handCard = cards;
        return this;
    }
    public CardPlayer setFeatures(List<Feature> features) {
        this.features = features;
        return this;
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = super.toJson();
        JSONArray array = JsonWrapper.collectionToJsonArray(handCard, new JSONArray());
        jobj.put(CardPlayer.class.getName() + "handCard", array);
        array = JsonWrapper.collectionToJsonArray(features, new JSONArray());
        jobj.put(CardPlayer.class.getName() + "features", array);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        super.fromJson(jobj);
        JSONArray array = jobj.getJSONArray(CardPlayer.class.getName() + "handCard");
        handCard =
            (List<Card>) JsonWrapper.jsonArrayToCollection(array, new ArrayList<Card>());
        array = jobj.getJSONArray(CardPlayer.class.getName() + "features");
        features =
            (List<Feature>) JsonWrapper.jsonArrayToCollection(array, new ArrayList<Feature>());

    }
}
