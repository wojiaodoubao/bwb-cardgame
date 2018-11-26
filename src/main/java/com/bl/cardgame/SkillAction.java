package com.bl.cardgame;

import com.bl.cardgame.cards.SkillCardGamblingPoints;

import java.util.List;

public class SkillAction extends PlayCardAction {
    static Card card = new SkillCardGamblingPoints();

    public SkillAction() {}

    public SkillAction(TYPE type, int srcPlayerIndex, List<Integer> targets) {
        super(type, srcPlayerIndex, -1, targets);
    }

    public Card getCard() {
        return card;
    }

    @Override
    public int getCardIndex() {
        return -1;
    }
}
