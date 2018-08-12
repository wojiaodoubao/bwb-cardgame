package com.bl.serialization;

import com.bl.Action;
import com.bl.Game;
import com.bl.Player;
import com.bl.cardgame.*;
import com.bl.cardgame.cards.Card1;
import com.bl.cardgame.cards.PlaceHolderCard;
import com.bl.rpc.Packet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Serializer {
    // TODO:序列化整个packet!
    public static final String PLAYER_DEAD_ACTION = "PlayerDeadAction";
    public static final String DRAW_CARD_ACTION = "DrawCardAction";
    public static final String SHUFFLE_CARD_ACTION = "ShuffleCardAction";
    public static final String PLAY_CARD_ACTION = "PlayCardAction";
    public static final String SKILL_ACTION = "SkillAction";
    public static final String EXCHANGE_CARD_ACTION = "ExchangeCardAction";
    public static final String Get_Game_Exception = "GetGameException";
    private static final PlaceHolderCard placeHolderCard = new PlaceHolderCard();
    public static String cardToJson(Card card) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("point", card.getPoint());
        if (card instanceof Card1) {
            jsonObj.put("type", "Card1");
        } else if (card instanceof PlaceHolderCard) {
            jsonObj.put("type", "PlaceHolder");
        }
        return jsonObj.toString();
    }
    public static Card jsonToCard(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        int point = jsonObj.getInt("point");
        if (jsonObj.get("type").equals("Card1")) {
            return new Card1(point);
        } else if (jsonObj.get("type").equals("PlaceHolder")) {
            return placeHolderCard;
        } else {
            throw new RuntimeException("Bad card type!");
        }
    }
    public static String featureToJson(Feature feature) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        if (feature instanceof DisableSkillFeature) {
            jsonObj.put("type", "DisableSkillFeature");
        } else if (feature instanceof ProtectedFeature) {
            jsonObj.put("type", "ProtectedFeature");
        }
        return jsonObj.toString();
    }
    public static Feature jsonToFeature(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        if (jsonObj.get("type").equals("DisableSkillFeature")) {
            return new DisableSkillFeature();
        } else if (jsonObj.get("type").equals("ProtectedFeature")) {
            return new ProtectedFeature();
        } else {
            throw new RuntimeException("Bad feature");
        }
    }
    public static String playerToJson(Player player) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("alive", player.isAlive());
        jsonObj.put("name", player.getName());
        jsonObj.put("id", player.getId());
        if (player instanceof CardPlayer) {
            jsonObj.put("type", "CardPlayer");
            CardPlayer cardPlayer = (CardPlayer) player;
            JSONArray jsonArray = new JSONArray();
            for (Card card : cardPlayer.getHandCards()) {
                jsonArray.put(cardToJson(card));
            }
            jsonObj.put("handCards", jsonArray);
            jsonArray = new JSONArray();
            for (Feature feature : cardPlayer.getFeatures()) {
                jsonArray.put(featureToJson(feature));
            }
            jsonObj.put("features", jsonArray);
        }
        return jsonObj.toString();
    }
    public static Player jsonToPlayer(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        Player player = null;
        if (jsonObj.get("type").equals("CardPlayer")) {
            JSONArray jsonArray = jsonObj.getJSONArray("handCards");
            List<Card> handCards = new ArrayList<Card>();
            for (int i=0;i<jsonArray.length();i++) {
                handCards.add(jsonToCard(jsonArray.getString(i)));
            }
            jsonArray = jsonObj.getJSONArray("features");
            List<Feature> features = new ArrayList<Feature>();
            for (int i=0;i<jsonArray.length();i++) {
                features.add(jsonToFeature(jsonArray.getString(i)));
            }
            player = new CardPlayer().setHandCards(handCards).setFeatures(features);
        } else {
            throw new RuntimeException("Bad Player");
        }
        boolean alive = jsonObj.getBoolean("alive");
        String name = jsonObj.getString("name");
        int id = jsonObj.getInt("id");
        player.setAlive(alive).setName(name).setId(id);
        return player;
    }
    public static String gameToJson(Game game) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        if (game instanceof CardGame) {
            jsonObj.put("type", "CardGame");
            CardGame cgame = (CardGame) game;
            jsonObj.put("phase", cgame.getPhase());
            jsonObj.put("gameOver", cgame.getGameOver());
            jsonObj.put("aliveNum", cgame.getAliveNum());
            jsonObj.put("curPlayerIndex", cgame.getCurPlayerIndex());
            JSONArray jsonArray = new JSONArray();
            for (Player player : cgame.getPlayerList()) {
                jsonArray.put(playerToJson(player));
            }
            jsonObj.put("playerList", jsonArray);
            jsonArray = new JSONArray();
            for (CardAction action : cgame.getActQueue()) {
                jsonArray.put(actionToJson(action));
            }
            jsonObj.put("actQueue", jsonArray);
            jsonArray = new JSONArray();
            for (Card card : cgame.getCardList()) {
                jsonArray.put(cardToJson(card));
            }
            jsonObj.put("cardList", jsonArray);
            jsonArray = new JSONArray();
            for (Card card : cgame.getDiscardCardList()) {
                jsonArray.put(cardToJson(card));
            }
            jsonObj.put("discardCardList", jsonArray);
            //TODO : 写完序列化写个game的序列化单测，绝对物超所值！
            //TODO : 然后把RPC单测写了，物超所值
            //TODO : 写一个命令行shell即可了就
        } else {
            throw new RuntimeException("Bad game");
        }
        return jsonObj.toString();
    }
    public static Game jsonToGame(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        if (jsonObj.get("type").equals("CardGame")) {
            CardGame cardGame = new CardGame();
            CardGame.PHASE phase = CardGame.PHASE.valueOf(jsonObj.getString("phase"));
            boolean gameOver = jsonObj.getBoolean("gameOver");
            int aliveNum = jsonObj.getInt("aliveNum");
            int curPlayerIndex = jsonObj.getInt("curPlayerIndex");
            JSONArray jsonArray = jsonObj.getJSONArray("playerList");
            List<CardPlayer> playerList = new ArrayList<CardPlayer>();
            for (int i=0;i<jsonArray.length();i++) {
                playerList.add((CardPlayer) jsonToPlayer(jsonArray.getString(i)));
            }
            jsonArray = jsonObj.getJSONArray("actQueue");
            List<CardAction> actionList = new ArrayList<CardAction>();
            for (int i=0;i<jsonArray.length();i++) {
                actionList.add((CardAction) jsonToAction(jsonArray.getString(i)));
            }
            jsonArray = jsonObj.getJSONArray("cardList");
            List<Card> cardList = new ArrayList<Card>();
            for (int i=0;i<jsonArray.length();i++) {
                cardList.add(jsonToCard(jsonArray.getString(i)));
            }
            jsonArray = jsonObj.getJSONArray("discardCardList");
            List<Card> discardCardList = new ArrayList<Card>();
            for (int i=0;i<jsonArray.length();i++) {
                discardCardList.add(jsonToCard(jsonArray.getString(i)));
            }
            cardGame.setPhase(phase).setGameOver(gameOver).setAliveNum(aliveNum)
                    .setCurPlayerIndex(curPlayerIndex).setPlayerList(playerList)
                    .setActQueue(actionList).setCardList(new LinkedList<Card>(cardList))
                    .setDiscardCardList(new LinkedList<Card>(discardCardList));
            return cardGame;
        } else {
            throw new RuntimeException("Bad game type");
        }
    }
    public static String actionToJson(Action action) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        if (action instanceof CardAction) {
            CardAction an = (CardAction) action;
            jsonObj.put("type",an.type);
            jsonObj.put("srcPlayerIndex",an.srcPlayerIndex);
            jsonObj.put("timestamp",an.timestamp);
        }
        if (action instanceof PlayerDeadAction) {
            jsonObj.put("actionType",PLAYER_DEAD_ACTION);
            PlayerDeadAction an = (PlayerDeadAction) action;
            jsonObj.put("playerIndex", an.getPlayerIndex());
            jsonObj.put("msg", an.getMsg());
        } else if (action instanceof DrawCardAction) {
            jsonObj.put("actionType",DRAW_CARD_ACTION);
            DrawCardAction an = (DrawCardAction) action;
            jsonObj.put("source", an.getSource());
        } else if (action instanceof ShuffleCardAction) {
            jsonObj.put("actionType",SHUFFLE_CARD_ACTION);
        } else if (action instanceof SkillAction) {
            jsonObj.put("actionType",SKILL_ACTION);
            SkillAction an = (SkillAction) action;
            jsonObj.put("cardIndex",an.getCardIndex());
            jsonObj.put("source",an.getSource());
            JSONArray array = intarrayToJSONArray(an.getTargets());
            jsonObj.put("targets", array);
        } else if (action instanceof PlayCardAction) {
            jsonObj.put("actionType",PLAY_CARD_ACTION);
            PlayCardAction an = (PlayCardAction) action;
            jsonObj.put("cardIndex",an.getCardIndex());
            jsonObj.put("source",an.getSource());
            JSONArray array = intarrayToJSONArray(an.getTargets());
            jsonObj.put("targets", array);
        } else if (action instanceof ExchangeCardAction) {
            jsonObj.put("actionType",EXCHANGE_CARD_ACTION);
            ExchangeCardAction an = (ExchangeCardAction) action;
            jsonObj.put("srcPlayerId", an.getSrcPid());
            jsonObj.put("dstPlayerId", an.getDstPid());
        } else if (action instanceof GetCardGameAction) {
            jsonObj.put("actionType", Get_Game_Exception);
        }
        return jsonObj.toString();
    }
    public static Action jsonToAction(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        Action action = null;
        if (jsonObj.get("actionType").equals(PLAYER_DEAD_ACTION)) {
            int playerIndex = jsonObj.getInt("playerIndex");
            String msg = jsonObj.getString("msg");
            action = new PlayerDeadAction(playerIndex, msg);
        } else if (jsonObj.get("actionType").equals(DRAW_CARD_ACTION)) {
            int srcPlayerIndex = jsonObj.getInt("srcPlayerIndex");
            int source = jsonObj.getInt("source");
            action = new DrawCardAction(srcPlayerIndex, source);
        } else if (jsonObj.get("actionType").equals(SHUFFLE_CARD_ACTION)) {
            action = new ShuffleCardAction();
        } else if (jsonObj.get("actionType").equals(SKILL_ACTION)) {
            int source = jsonObj.getInt("source");
            JSONArray array = jsonObj.getJSONArray("targets");
            int[] targets = jsonArrayTointarray(array);
            action = new SkillAction(null, -1, source, targets);// set CardAction attributes later
        } else if (jsonObj.get("actionType").equals(PLAY_CARD_ACTION)) {
            int source = jsonObj.getInt("source");
            int cardIndex = jsonObj.getInt("cardIndex");
            JSONArray array = jsonObj.getJSONArray("targets");
            int[] targets = jsonArrayTointarray(array);
            action = new PlayCardAction(null, -1, cardIndex, source, targets);// set CardAction attributes later
        } else if (jsonObj.get("actionType").equals(EXCHANGE_CARD_ACTION)) {
            int srcPid = jsonObj.getInt("srcPid");
            int dstPid = jsonObj.getInt("dstPid");
            action = new ExchangeCardAction(srcPid, dstPid);
        } else if (jsonObj.get("actionType").equals(Get_Game_Exception)) {
            action = new GetCardGameAction();
        } else {
            return null;
        }
        if (action instanceof CardAction) {
            CardAction.TYPE type = CardAction.TYPE.valueOf(jsonObj.getString("type"));
            long timestamp = jsonObj.getLong("timestamp");
            int srcPlayerIndex = jsonObj.getInt("srcPlayerIndex");
            CardAction cardAction = ((CardAction)action);
            cardAction.set(type,timestamp,srcPlayerIndex);
        }
        return action;
    }
    public static <T extends Object> JSONArray listToJSONArray(List<T> list) {
        JSONArray array = new JSONArray();
        for (T obj : list) {
            array.put(obj);
        }
        return array;
    }
    public static JSONArray intarrayToJSONArray(int[] list) {
        JSONArray array = new JSONArray();
        for (int obj : list) {
            array.put(obj);
        }
        return array;
    }
    public static int[] jsonArrayTointarray(JSONArray array) throws JSONException {
        int[] list = new int[array.length()];
        for (int i=0;i<array.length();i++) {
            list[i] = Integer.parseInt(array.getString(i));
        }
        return list;
    }

    public static byte[] gameTobyte(Game game) throws JSONException {
        return gameToJson(game).getBytes();
    }
    public static Game byteToGame(byte[] bytes) throws JSONException {
        String jsonStr = new String(bytes);
        return jsonToGame(jsonStr);
    }
    public static byte[] actionTobyte(Action action) throws JSONException {
        return actionToJson(action).getBytes();
    }
    public static Action byteToaction(byte[] bytes) throws JSONException {
        String jsonStr = new String(bytes);
        return jsonToAction(jsonStr);
    }

    public static byte[] packetTobyte(Packet packet) throws IOException {
        long pid = packet.getId();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bao);
        dos.writeLong(pid);
        dos.writeBoolean(packet.isAnyException());
        if (packet.getData() == null) {
            dos.writeInt(0);
        } else {
            dos.writeInt(packet.getData().length);
            dos.write(packet.getData());
        }
        if (packet.getRes() == null) {
            dos.writeInt(0);
        } else {
            dos.writeInt(packet.getRes().length);
            dos.write(packet.getRes());
        }
        dos.flush();
        return bao.toByteArray();
    }

    public static Packet byteTopacket(byte[] bytes) throws IOException {
        ByteArrayInputStream bai = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bai);
        long pid = dis.readLong();
        boolean exception = dis.readBoolean();
        int dataLen = dis.readInt();
        byte[] data = new byte[dataLen];
        read(data, dis);
        int resLen = dis.readInt();
        byte[] res = new byte[resLen];
        read(res, dis);
        Packet packet = new Packet().setId(pid).setException(exception)
                .setData(data).setRes(res);
        return packet;
    }

    private static void read(byte[] buf, InputStream in) throws IOException {
        int left = buf.length;
        while (left > 0) {
            int len = in.read(buf, buf.length-left, left);
            left -= len;
        }
    }
}
