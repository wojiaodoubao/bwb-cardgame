package com.bl.cardgame;

import com.bl.Action;
import com.bl.Player;

public class CardAction extends Action {
    public enum TYPE {
        NON_EFFECT,EFFECT
    }
    public TYPE type = TYPE.NON_EFFECT;
    public long timestamp;
    // srcPlayerIndex=-1 means srcPlayer comes from game, not a player. 比如玩家打出牌造成交换牌，那么交换牌action来自game
    public int srcPlayerIndex;// CardAction source is different from like PlayCardAction's src target nor ExchangeCardAction's src taret.
    public CardAction() {
        type = TYPE.NON_EFFECT;
        srcPlayerIndex = -1;
        this.timestamp = System.currentTimeMillis();
    }
    public CardAction(TYPE type, int srcPlayerIndex) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.srcPlayerIndex = srcPlayerIndex;
    }
    public boolean shoudRecord() {
        return true;
    }
    public int getSrcPlayer() {
        return this.srcPlayerIndex;
    }
    public TYPE getType() {
        return this.type;
    }
    public void set(TYPE type,long timestamp,int srcPlayerIndex) {
        this.type = type;
        this.timestamp = timestamp;
        this.srcPlayerIndex = srcPlayerIndex;
    }
}
