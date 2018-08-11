package com.bl.cardgame;

import com.bl.cardgame.cards.SkillCardGamblingPoints;

public class SkillAction extends PlayCardAction {
    static Card card = new SkillCardGamblingPoints();
    public SkillAction(TYPE type, int srcPlayerIndex, int source, int[] targets) {
        super(type, srcPlayerIndex, -1, source, targets);
    }
    public Card getCard() {
        return card;
    }
    @Override
    public int getCardIndex() {
        return -1;
    }
}
