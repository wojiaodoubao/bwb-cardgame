//package com.bl.cardgame;
//
//import com.bl.cardgame.cards.Card1;
//import com.bl.rpc.RPCClient;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//public class CardGameCommandClient implements Runnable {
//    public static void main(String args[]) throws InterruptedException {
////        CardGameCommandClient client = new CardGameCommandClient();
////        Thread gameT = new Thread(client);
////        gameT.start();
////        gameT.join();
//    }
//    private int playerId;
//    private RPCClient client;
//    public CardGameCommandClient(int playerId, String host, int port) throws IOException {
//        this.playerId = playerId;
//        this.client = new RPCClient(host, port);
//    }
//    @Override
//    public void run() {
//        Scanner sc = new Scanner(System.in);
//        String command;
//        while (true) {
//            try {
//                System.out.println("请输入命令:");
//                command = sc.nextLine();
//                CardAction action = parseCommand(command);
//                CardGame cardGame = (CardGame) client.doAction(action);
//                if (!cardGame.getPlayer(playerId).isAlive()) break;
//            } catch (Exception e) {
//                System.out.println("Got error:" + e.getMessage());
//            }
//        }
//        System.out.println("Game over!");
//        try {
//            client.close();
//        } catch (IOException e) {
//        }
//    }
//    private void formatOutputGame(CardGame game) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("");
////        game.
//    }
//    private CardAction parseCommand(String command) {
//        // [draw]
//        // [play] cardIndex target effect
//        // [skill] target effect
//        String[] items = command.split(" ");
//        CardAction cardAction = null;
//        if (items[0].equals("draw")) {
//            cardAction = new DrawCardAction(playerId, playerId);
//        } else if (items[0].equals("play")) {
//            int cardIndex = Integer.parseInt(items[1]);
//            int[] target = new int[1];
//            target[0] = Integer.parseInt(items[2]);
//            CardAction.TYPE type = items[3].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
//            cardAction = new PlayCardAction(type, playerId, cardIndex, playerId, target);
//        } else if (items[0].equals("skill")) {
//            int[] target = new int[1];
//            target[0] = Integer.parseInt(items[1]);
//            CardAction.TYPE type = items[2].equals("effect") ? CardAction.TYPE.EFFECT : CardAction.TYPE.NON_EFFECT;
//            cardAction = new SkillAction(type, playerId, playerId, target);
//        } else if (items[0].equals("show")) {
//            return new GetCardGameAction();
//        }
//        return cardAction;
//    }
//}
