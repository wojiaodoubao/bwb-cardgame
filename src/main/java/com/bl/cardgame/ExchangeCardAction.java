package com.bl.cardgame;

public class ExchangeCardAction extends CardAction {
    int srcPid;
    int dstPid;
    public ExchangeCardAction(int srcPlayerId, int dstPlayerId) {
        super();
        this.srcPid = srcPlayerId;
        this.dstPid = dstPlayerId;
    }
    public int getSrcPid() {
        return srcPid;
    }
    public int getDstPid() {
        return dstPid;
    }
}
