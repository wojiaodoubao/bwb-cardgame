package com.bl.cardgame;

import com.bl.Action;

public class PlayCardAction extends CardAction {
    int cardIndex;
    int source;
    int[] targets;
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
}
