package com.bl.cardgame;

import com.bl.Action;
import com.bl.Player;

import java.util.List;
import java.util.Random;

public abstract class Card {
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
        CardPlayer player = game.getPlayer(action.getSource());
        player.removeCard(action.cardIndex);
        game.addToDiscardCard(this);
    }

    public int getPoint() {
        return point;
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
