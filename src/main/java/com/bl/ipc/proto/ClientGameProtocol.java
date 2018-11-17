package com.bl.ipc.proto;

import com.bl.Action;
import com.bl.Action.UnknownActionException;
import com.bl.Game;
import com.bl.Player;

import java.io.IOException;

import com.bl.cardgame.Card.SanityException;
import com.bl.cardgame.CardGame.GameException;

public interface ClientGameProtocol {
  public Game doAction(Action action) throws UnknownActionException,
      GameException, SanityException, IOException;
  public Game getGame(Player player) throws UnknownActionException,
      GameException, SanityException, IOException;
}
