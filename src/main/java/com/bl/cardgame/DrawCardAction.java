package com.bl.cardgame;

import com.bl.Action;

public class DrawCardAction extends CardAction {
    int source;
    public DrawCardAction(int source) {
        this(-1, source);// action comes from game
    }
    public DrawCardAction(int srcPlayerIndex, int source) {
        super(TYPE.NON_EFFECT, srcPlayerIndex);
        this.source = source;
    }
    public int getSource() {
        return source;
    }
}
