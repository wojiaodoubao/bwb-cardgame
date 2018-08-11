package com.bl;

import com.bl.cardgame.Card;
import com.bl.cardgame.CardGame;

import java.io.IOException;

public interface ClientGameProtocol {
    public Game doAction(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Game getGame(Player player) throws Card.SanityException, Action.UnknownActionException, CardGame.GameException, IOException;
}
