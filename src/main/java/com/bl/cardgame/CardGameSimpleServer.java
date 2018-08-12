package com.bl.cardgame;

import com.bl.Action;
import com.bl.Player;
import com.bl.cardgame.cards.Card1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CardGameSimpleServer implements Runnable {
    private CardGame game;
    public CardGameSimpleServer(CardGame cgame) {
        this.game = cgame;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        String command;
        while (!game.isGameOver()) {
            try {
                System.out.println("请输入命令:");
                command = sc.nextLine();
                Result res = processCommand(command);
                CardAction action = res.getCaction();
                if (action instanceof ShowAction) {
                    String[] cmd = ((ShowAction) action).getCommand();
                    if (cmd.length > 0) {
                        if (cmd[0].equals("basic")) {
                            System.out.println(game.to_string_basic());
                        } else if (cmd[0].equals("player")) {
                            if (cmd.length == 2) {
                                System.out.println(game.to_string_player(Integer.parseInt(cmd[1])));
                            } else {
                                System.out.println(game.to_string_all_player());
                            }
                        } else if (cmd[0].equals("handcard")) {
                            System.out.println(game.to_string_hand_cards());
                        } else if (cmd[0].equals("cardlist")) {
                            System.out.println(game.to_string_card_list());
                        } else if (cmd[0].equals("actionlist")) {
                            System.out.println(game.to_string_act_queue());
                        } else if (cmd[0].equals("all")) {
                            System.out.println(game.getGame(null));
                        }
                    }
                    continue;
                }
                try {
                    game.doAction(action);
                } catch (Action.UnknownActionException e) {
                    System.out.println("Error:" + e.getMessage());
                } catch (CardGame.GameException e) {
                    System.out.println("Error:" + e.getMessage());
                } catch (Card.SanityException e) {
                    System.out.println("Error:" + e.getMessage());
                }
//            outputGame(game.getGame(res.getCplayer()));
//            System.out.println(game.getGame(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.close();
    }

    private class Result {
        CardAction caction;
        CardPlayer cplayer;
        public Result(CardAction caction, CardPlayer cplayer) {
            this.caction = caction;
            this.cplayer = cplayer;
        }
        public CardPlayer getCplayer() {
            return cplayer;
        }
        public CardAction getCaction() {
            return caction;
        }
    }
    private class ShowAction extends CardAction {
        String[] command;
        public ShowAction(String[] command) {
            this.command = command;
        }
        public String[] getCommand() {
            return command;
        }
    }
    private Result processCommand(String command) {
        // playerId [draw] source
        //          [play] cardIndex source target effect
        //          [skill] source target effect
        String[] items = command.split(" ");
        int playerId = Integer.parseInt(items[0]);
        CardAction cardAction = null;
        if (items[1].equals("draw")) {
            int source = Integer.parseInt(items[2]);
            cardAction = new DrawCardAction(playerId, source);
        } else if (items[1].equals("play")) {
            int cardIndex = Integer.parseInt(items[2]);
            int source = Integer.parseInt(items[3]);
            int[] target = new int[1];
            target[0] = Integer.parseInt(items[4]);
            CardAction.TYPE type = items[5].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
            cardAction = new PlayCardAction(type, playerId, cardIndex, source, target);
        } else if (items[1].equals("skill")) {
            int source = Integer.parseInt(items[2]);
            int[] target = new int[1];
            target[0] = Integer.parseInt(items[3]);
            CardAction.TYPE type = items[4].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
            cardAction = new SkillAction(type, playerId, source, target);
        } else if (items[1].equals("show")) {
            String[] cmd = new String[items.length-2];
            for (int i=2;i<items.length;i++) {
                cmd[i-2] = items[i];
            }
            cardAction = new ShowAction(cmd);
        }
        CardPlayer player = game.getPlayer(playerId);
        return new Result(cardAction, player);
    }

    private void outputGame(CardGame game) {

    }

    public static void main(String args[]) throws InterruptedException {
        List<CardPlayer> playerList = new ArrayList<CardPlayer>();
        List<Card> cards = new ArrayList<Card>();
        for (int i=0;i<10;i++) {
            cards.add(new Card1(i));
        }
        for (int j=0;j<3;j++) {
            playerList.add(new CardPlayer(j, "p"+j));
        }
        CardGame cgame = new CardGame(playerList, cards);
        CardGameSimpleServer server = new CardGameSimpleServer(cgame);
        Thread gameT = new Thread(server);
        gameT.start();
        gameT.join();
    }
}
