package com.bl.cardgame;

import com.bl.ipc.jason.JsonWrapper;
import com.bl.ipc.jason.JsonWritable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public abstract class Card implements JsonWritable {
    protected int point;
    public static class SanityException extends Exception {
        public SanityException(String msg) {
            super(msg);
        }
    }
    // sanity check, should be done in both client & server
    public void sanityCheck(CardAction action, CardGame game) throws SanityException {
        if (action instanceof PlayCardAction) {
            PlayCardAction pa = (PlayCardAction) action;
            for (int target : pa.targets) {
                if (game.getPlayer(target) == null ||
                        !game.getPlayer(target).isAlive()) {
                    throw new SanityException("Couldn't choose dead player as target!" +
                            " PlayerId:"+target+
                            " PlayerName:"+game.getPlayer(target).getName());
                }
            }
        }
    }

    public Card() {}

    public Card(int point) {
        this.point = point;
    }

    // draw effect
    public void draw(CardGame game,int curPlayerIndex) throws CardGame.GameException {

    }
    // action effect
    public void play(PlayCardAction pa, CardGame game) throws CardGame.GameException {

    }
    // discard effect
    public void discard(PlayCardAction action,CardGame game) throws CardGame.GameException {
        CardPlayer player = game.getPlayer(action.getSrcPlayer());
        player.removeCard(action.cardIndex);
        game.addToDiscardCard(this);
    }

    public int getPoint() {
        return point;
    }

    @Override
    public int hashCode() {
        return point;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Card) {
            Card card = (Card) obj;
            return card.point == point;
        }
        return false;
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = new JSONObject();
        JsonWrapper.objectToJson(this.getClass(), this, jobj);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        JsonWrapper.jsonToObject(this.getClass(), this, jobj);
    }

    // card operate
    public static void shuffleCards(List<Card> cardList) {
        if (cardList == null) return;
        Random random = new Random(System.currentTimeMillis());
        for (int i=0;i<cardList.size();i++) {
            int index = random.nextInt(cardList.size()-i)+i;
            Card tmpCard = cardList.get(i);
            cardList.set(i,cardList.get(index));
            cardList.set(index,tmpCard);
        }
    }
}
