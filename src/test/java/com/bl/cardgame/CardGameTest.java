package com.bl.cardgame;

import com.bl.cardgame.cards.Card1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * testing logic
 * */
public class CardGameTest implements Runnable {
    private CardGame game;
    public CardGameTest(CardGame cgame) {
        this.game = cgame;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        String command;
        while (!game.isGameOver()) {
            try {
              System.out.println("请输入命令:");
              command = sc.nextLine();
              // playerId [draw] source
              //          [play] cardIndex source target effect
              //          [skill] source target effect
              String[] items = command.split(" ");
              int playerId = Integer.parseInt(items[0]);
              if (items[1].equals("draw")) {
                game.drawCard(new DrawCardAction(playerId));
              } else if (items[1].equals("play")) {
                int cardIndex = Integer.parseInt(items[2]);
                int[] target = new int[1];
                target[0] = Integer.parseInt(items[3]);
                CardAction.TYPE type = items[4].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
                game.playCard(new PlayCardAction(type, playerId, cardIndex, target));
              } else if (items[1].equals("skill")) {
                int[] target = new int[1];
                target[0] = Integer.parseInt(items[2]);
                CardAction.TYPE type = items[3].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
                game.skill(new SkillAction(type, playerId, target));
              } else if (items[1].equals("show")) {
                if (items.length > 1) {
                  if (items[2].equals("basic")) {
                    System.out.println(game.to_string_basic());
                  } else if (items[2].equals("player")) {
                    if (items.length == 4) {
                      System.out.println(game.to_string_player(Integer.parseInt(items[3])));
                    } else {
                      System.out.println(game.to_string_all_player());
                    }
                  } else if (items[2].equals("handcard")) {
                    System.out.println(game.to_string_hand_cards());
                  } else if (items[2].equals("cardlist")) {
                    System.out.println(game.to_string_card_list());
                  } else if (items[2].equals("actionlist")) {
                    System.out.println(game.to_string_act_queue());
                  } else if (items[2].equals("all")) {
                    System.out.println(game.getGame(null));
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
        }
        sc.close();
    }

    public static void main(String args[]) throws InterruptedException {
      CardGame.ReturnWholeCardGame = true;
      List<CardPlayer> playerList = new ArrayList<CardPlayer>();
      List<Card> cards = new ArrayList<Card>();
      for (int i=0;i<10;i++) {
          cards.add(new Card1(i));
      }
      for (int j=0;j<3;j++) {
          playerList.add(new CardPlayer(j, "p"+j));
      }
      CardGame cgame = new CardGame(playerList, cards);
      CardGameTest server = new CardGameTest(cgame);
      Thread gameT = new Thread(server);
      gameT.start();
      gameT.join();
    }
}
