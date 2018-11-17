package com.bl;

import com.bl.cardgame.Card;
import com.bl.cardgame.CardGame;

import java.io.IOException;

public interface ClientGameProtocol {
    // every method should has a return value, so we don't need additional code
    // and keep it simple
    // TODO:不要一个接口发送所有action，搞多个方法
    public Game doAction(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Game getGame(Player player) throws Card.SanityException, Action.UnknownActionException, CardGame.GameException, IOException;
    public Action foo(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Player foo(Player player) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
}
