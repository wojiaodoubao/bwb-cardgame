package com.bl.ipc.proto;

import com.bl.Action;
import com.bl.Game;
import com.bl.Player;
import com.bl.cardgame.*;

import java.io.IOException;

public interface ClientGameProtocol {
    // every method should has a return value, so we don't need additional code
    // and keep it simple
    public Game getGame(Player player) throws Card.SanityException,
        Action.UnknownActionException, CardGame.GameException, IOException;
    public Game playCard(PlayCardAction action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Game drawCard(DrawCardAction action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Game skill(SkillAction action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Action foo(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
    public Player foo(Player player) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException, IOException;
}
