package com.bl.cardgame.cards;

import com.bl.cardgame.*;

public class SkillCardGamblingPoints extends Card {
    public SkillCardGamblingPoints() {
        super(-1);
    }
    @Override
    public void play(PlayCardAction pa, CardGame game) {
        // we should do some check to make it strong, but now
        // just do it
        assert pa.getType() == CardAction.TYPE.EFFECT;
        // player shouldn't be null
        CardPlayer srcPlayer = game.getPlayer(pa.getSrcPlayer());
        CardPlayer dstPlayer = game.getPlayer(pa.getTargets().get(0));
        // both src and dst should have only one card in hand.
        Card srcCard = srcPlayer.getCard(0);
        Card dstCard = dstPlayer.getCard(0);
        if (srcCard.getPoint() < dstCard.getPoint()) {
            game.killPlayer(srcPlayer);
        } else if (srcCard.getPoint() > dstCard.getPoint()) {
            game.killPlayer(dstPlayer);
        } else {

        }
    }
    @Override
    public void discard(PlayCardAction action,CardGame game) {
        // skill card do nothing in discard.
    }
}
