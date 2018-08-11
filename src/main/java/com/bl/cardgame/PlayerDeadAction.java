package com.bl.cardgame;

public class PlayerDeadAction extends CardAction {
    int playerIndex;
    String msg;
    public PlayerDeadAction(int playerIndex, String msg) {
        super();
        this.playerIndex = playerIndex;
        this.msg = msg;
    }
    public int getPlayerIndex() {
        return playerIndex;
    }
    public String getMsg() {
        return msg;
    }
}
