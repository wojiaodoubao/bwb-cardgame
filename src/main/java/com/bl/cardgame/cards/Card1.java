package com.bl.cardgame.cards;

import com.bl.cardgame.*;
import com.bl.cardgame.CardAction.TYPE;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

// exchange card, then you can't use gambling skill this round.
public class Card1 extends Card {
    public Card1() {}
    public Card1(int point) {
        super(point);
    }
    @Override
    public void play(PlayCardAction pa, CardGame game) throws CardGame.GameException {
        if (pa.getType() == TYPE.NON_EFFECT) {
            return;
        } else {
            CardPlayer srcPlayer = game.getPlayer(pa.getSrcPlayer());
            CardPlayer dstPlayer = game.getPlayer(pa.getTargets().get(0));
            Card srcCard = srcPlayer.removeCard(1-pa.getCardIndex());
            Card dstCard = dstPlayer.removeCard(0);
            srcPlayer.insertCard(1-pa.getCardIndex(), dstCard);
            dstPlayer.addCard(srcCard);
            srcPlayer.addFeature(new DisableSkillFeature());
            game.addAction(new ExchangeCardAction(pa.getSrcPlayer(),
                pa.getTargets().get(0)));
        }
    }
}
