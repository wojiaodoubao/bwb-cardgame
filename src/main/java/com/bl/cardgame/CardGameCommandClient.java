package com.bl.cardgame;

import com.bl.Action;
import com.bl.ipc.jason.JsonRpcClient;
import com.bl.ipc.proto.ClientGameProtocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class CardGameCommandClient {

  public static final Log LOG = LogFactory.getLog(CardGameCommandClient.class);

  private int playerId;
  private CardPlayer player;
  private ClientGameProtocol client;
  private CardGame game;
  private long lastShowTime;

  public CardGameCommandClient(int playerId, InetSocketAddress address, long timeout)
      throws IOException, CardGame.GameException, Action.UnknownActionException,
      Card.SanityException {
    this.playerId = playerId;
    this.client = (ClientGameProtocol) JsonRpcClient
        .createProtocolImpl(ClientGameProtocol.class, address, timeout);
    this.game = (CardGame) client.getGame(new CardPlayer(playerId, ""));
    this.player = game.getPlayer(playerId);
  }

  public void play() {
    Scanner sc = new Scanner(System.in);
    String command;
    while (game == null || !game.isGameOver()) {
      try {
        System.out.println("------Input please(Usage)--------\n"
            + "[draw] | "
            + "[play] cardIndex target | "
            + "[discard] cardIndex | "
            + "[skill] target | "
            + "[skip] | "
            + "[show]");
        command = sc.nextLine();
        String[] items = command.split(" ");
        if (items[0].equals("draw")) {
          game = (CardGame) client.drawCard(new DrawCardAction(playerId));
        } else if (items[0].equals("play")) {
          int cardIndex = Integer.parseInt(items[1]);
          ArrayList<Integer> target = new ArrayList<>();
          target.add(Integer.parseInt(items[2]));
          CardAction.TYPE type = CardAction.TYPE.EFFECT;
          game = (CardGame) client.playCard(new PlayCardAction(type, playerId, cardIndex, target));
        } else if (items[0].equals("discard")) {
          int cardIndex = Integer.parseInt(items[1]);
          ArrayList<Integer> target = new ArrayList<>();
          target.add(Integer.parseInt(items[2]));
          CardAction.TYPE type = CardAction.TYPE.NON_EFFECT;
          game = (CardGame) client.playCard(new PlayCardAction(type, playerId, cardIndex, target));
        } else if (items[0].equals("skill")) {
          ArrayList<Integer> target = new ArrayList<>();
          target.add(Integer.parseInt(items[1]));
          CardAction.TYPE type = CardAction.TYPE.EFFECT;
          game =
              (CardGame) client.skill(new SkillAction(type, playerId, target));
        } else if (items[0].equals("skip")) {
          ArrayList<Integer> target = new ArrayList<>();
          target.add(Integer.parseInt(items[1]));
          CardAction.TYPE type = CardAction.TYPE.NON_EFFECT;
          game =
              (CardGame) client.skill(new SkillAction(type, playerId, target));
        } else if (items[0].equals("show")) {
          game = (CardGame) client.getGame(player);
        }
        showGame(true);
      } catch (Throwable e) {
        LOG.warn("Error while playing", e);
      }
    }
    System.out.println("Game over!");
  }

  public synchronized void showGame(boolean force) {
    System.out.print("\n\n\n\n\n\n\n\n\n\n");
    long now = System.currentTimeMillis();
    if (!force && now - lastShowTime < 10*1000) {
      return;
    }
    try {
      game = (CardGame) client.getGame(player);
    } catch (Exception e) {
      LOG.warn("Failed get game when show game.", e);
      return;
    }
    CardPlayer curPlayer = game.getPlayer(game.getCurPlayerIndex());
    int totalCardNum = game.getCardList().size() + game.getDiscardCardList().size();
    for (CardPlayer p : game.getPlayerList()) {
      totalCardNum += p.handCard.size();
    }
    System.out.println("-------Happy card-------");
    System.out.println(String.format("phase                  :%s", game.getPhase()));
    System.out.println(String.format("player(alive/total)    :%d/%d", game.aliveNum, game.getPlayerList().size()));
    System.out.println(String.format("cards (left/used/total):%d/%d/%d", game.getCardList().size(), game.getDiscardCardList().size(), totalCardNum));
    System.out.println(String.format("act   (size)           :%d", game.getActQueue().size()));
    System.out.println(String.format("cur-p (id/name/size)   :%d/%s/%d", curPlayer.getId(), curPlayer.getName(), curPlayer.handCard.size()));
    System.out.println("-------Desk Info---------");
    System.out.print(game.to_string_all_player());
    System.out.println("-------Hand Cards--------");
    System.out.print(game.to_string_hand_cards());
    System.out.println("-------Action History-------");
    System.out.print(game.to_string_act_queue());
  }

  public static void main(String args[]) throws Throwable {
    BasicConfigurator.configure();
    CardGameCommandClient client = new CardGameCommandClient(
        0, CardGame.address, 1000*3);
    client.play();
  }
}
