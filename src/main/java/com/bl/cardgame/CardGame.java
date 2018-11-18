package com.bl.cardgame;

import com.bl.Action;
import com.bl.Game;
import com.bl.Player;
import com.bl.cardgame.cards.PlaceHolderCard;
import com.bl.ipc.jason.JsonWritable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 比如说，我抓牌后内存都改对了，但是返回失败了，这个错误咱咋处理？要不就容错都放client，不一致就一次次重试
 * 再比如说，怎么防止两个client同时声称是同一个client？
 * 这个都慢慢以后再处理吧
 * */
public class CardGame extends Game implements JsonWritable {

    public static class GameException extends Exception {
        public GameException(String msg) {
            super(msg);
        }
    }
    public static enum PHASE {
        START_PHASE,DRAW_PHASE,PLAY_PHASE,END_PHASE
    }
    private List<CardPlayer> playerList = null;
    private LinkedList<Card> cardList = null;
    private LinkedList<Card> discardCardList = null;
    private List<CardAction> actQueue = null;
    private PHASE phase;
    private boolean gameOver;
    int aliveNum;
    int curPlayerIndex;
    public static boolean ReturnWholeCardGame = false;// for debug only
    private static final Card placeHolderCard = new PlaceHolderCard();
    //先写着，以后抽牌要是代码太冗余了再抽象一个CardGroup来专门负责洗牌和抓下一张牌等，到时候ShuffleCards可以放在CardGroup里
    public CardGame(List<CardPlayer> playerList,List<Card> cardList) throws IllegalArgumentException{
        if (playerList==null||cardList==null) {
            throw new IllegalArgumentException("List is null!");
        }
        this.playerList = playerList;
        this.cardList = new LinkedList<Card>(cardList);
        this.discardCardList = new LinkedList<Card>();
        this.actQueue = new ArrayList<CardAction>();
        initGame();
    }
    public CardGame() {}

    private void initGame() {
        aliveNum = playerList.size();
        curPlayerIndex = 0;
        phase = PHASE.START_PHASE;
        gameOver = false;
        shuffleCards();
        if (cardList.size() < playerList.size()) {
            throw new RuntimeException("card num is short of player num");
        }
        // deal card & set alive
        for (int playerNo=0;playerNo<playerList.size();playerNo++) {
            CardPlayer player = playerList.get(playerNo);
            player.setAlive(true);
            player.addCard(nextCard());
            actQueue.add(new DrawCardAction(playerNo));
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public CardPlayer getPlayer(int index) {
        if (index<0 || index >= playerList.size()) {
            return null;
        }
        return playerList.get(index);
    }

    public void addAction(CardAction action) {
        actQueue.add(action);
    }

    public CardPlayer killPlayer(int index) {
        CardPlayer player = getPlayer(index);
        return killPlayer(player);
    }

    public CardPlayer killPlayer(CardPlayer player) {
        for (int i=0; i<playerList.size(); i++) {
            CardPlayer pl = playerList.get(i);
            if (!pl.isAlive()) continue;
            if (player != null && (player == pl || player.equals(pl))) {
                pl.setAlive(false);
                aliveNum -- ;
                actQueue.add(new PlayerDeadAction(i,""));
                return pl;
            }
        }
        return null;
    }

    public CardPlayer currentPlayer() {
        return playerList.get(curPlayerIndex);
    }

    @Override
    public synchronized CardGame getGame(Player player) throws GameException {
        if (player == null || player.getId() < 0 || player.getId() >= playerList.size()) {
            if (ReturnWholeCardGame) {
                return this;
            } else {
                throw new GameException("Bad player Index!");
            }
        } else {
            CardGame ret = new CardGame();
            ret.setCurPlayerIndex(this.getCurPlayerIndex());
            ret.setAliveNum(this.getAliveNum());
            ret.setPhase(this.phase);
            ret.setGameOver(this.gameOver);
            ret.setActQueue(this.actQueue);
            ret.setDiscardCardList(this.discardCardList);
            LinkedList<Card> pcardList = new LinkedList<Card>();
            for (int i=0;i<cardList.size();i++) {
                pcardList.add(placeHolderCard);
            }
            ret.setCardList(pcardList);
            List<CardPlayer> pplayerList = new ArrayList<CardPlayer>();
            for (int i=0;i<playerList.size();i++) {
                CardPlayer pl = playerList.get(i);
                if (pl.getId() == player.getId()) {
                    pplayerList.add(pl);
                } else {
                    CardPlayer p = new CardPlayer();
                    p.setAlive(pl.isAlive());
                    p.setId(pl.getId());
                    p.setName(pl.getName());
                    p.setFeatures(pl.getFeatures());
                    List<Card> handC = new ArrayList<Card>();
                    for (int j = 0; j < pl.handCard.size(); j++) {
                        handC.add(placeHolderCard);
                    }
                    p.setHandCards(handC);
                    pplayerList.add(p);
                }
            }
            ret.setPlayerList(pplayerList);
            return ret;
        }
    }

    @Override
    public Game playCard(PlayCardAction action)
        throws Action.UnknownActionException, GameException,
        Card.SanityException, IOException {
        checkTurn(action);
        if (phase != PHASE.PLAY_PHASE) {
            throw new GameException("Wrong Phase");
        }
        if (currentPlayer().getCard(action.getCardIndex()) != null) {
            Card card = currentPlayer().getCard(action.getCardIndex());
            card.sanityCheck(action, this);
            card.play(action, this);
            card.discard(action, this);
        }
        enterPhase(PHASE.END_PHASE);
        recordAction(action);
        return getGame(currentPlayer());
    }

    @Override
    public Game drawCard(DrawCardAction action)
        throws Action.UnknownActionException, GameException,
        Card.SanityException, IOException {
        checkTurn(action);
        if (phase != PHASE.DRAW_PHASE && phase != PHASE.START_PHASE) {
            throw new GameException("Wrong Phase");
        }
        Card card = nextCardWithShuffle();
        card.draw(this,curPlayerIndex);// trigger draw effect
        currentPlayer().addCard(card);
        enterPhase(PHASE.PLAY_PHASE);
        recordAction(action);
        return getGame(currentPlayer());
    }

    @Override public Game skill(SkillAction action)
        throws Action.UnknownActionException, GameException,
        Card.SanityException, IOException {
        checkTurn(action);
        if (phase != PHASE.END_PHASE) {
            throw new GameException("Wrong Phase, Expect EndPhase");
        }
        if (action.getType() == CardAction.TYPE.EFFECT) {
            if (currentPlayer().getFeature(DisableSkillFeature.class) != null) {
                throw new GameException("Skill is disabled in this phase");
            }
            assert action.getCard() != null;
            Card card = action.getCard();
            card.sanityCheck(action, this);
            card.play(action, this);
        }
        currentPlayer().removeFeature(DisableSkillFeature.class);
        enterPhase(PHASE.START_PHASE);
        recordAction(action);
        return getGame(currentPlayer());
    }

    private void checkTurn(CardAction action) throws GameException {
        Player player = getPlayer(action.getSrcPlayer());
        if (!currentPlayer().equals(player)) {
            throw new GameException("Not your turn!");
        }
    }

    private void recordAction(CardAction action) {
        if (action.shoudRecord()) {
            actQueue.add(action);
        }
    }

    private void shuffleCards() {
        Card.shuffleCards(cardList);
        actQueue.add(new ShuffleCardAction());
    }

    private Card nextCardWithShuffle() {
        Card card = nextCard();
        if (card == null) {
            while (discardCardList.size()>0) {
                cardList.addLast(discardCardList.removeFirst());
            }
            shuffleCards();
            card = nextCard();
        }
        return card;
    }

    private Card nextCard() {
        if (cardList.size() == 0) {
            return null;
        } else {
            return cardList.removeFirst();
        }
    }

    public void addToDiscardCard(Card card) {
        discardCardList.addLast(card);
    }

    private void enterPhase(PHASE newPhase) {
        if (newPhase == PHASE.START_PHASE) {
            // Round开始，检查保护标记(标记上记round数)，如果有就去掉
            // Round开始，要curPlayer++
            curPlayerIndex = (curPlayerIndex + 1) % aliveNum;
        } else if (newPhase == PHASE.DRAW_PHASE) {

        } else if (newPhase == PHASE.PLAY_PHASE) {

        } else if (newPhase == PHASE.END_PHASE) {

        }
        phase = newPhase;
    }

    public List<CardPlayer> getPlayerList() {
        return playerList;
    }

    public LinkedList<Card> getCardList() {
        return cardList;
    }

    public LinkedList<Card> getDiscardCardList() {
        return discardCardList;
    }

    public int getAliveNum() {
        return aliveNum;
    }

    public int getCurPlayerIndex() {
        return curPlayerIndex;
    }

    public List<CardAction> getActQueue() {
        return actQueue;
    }

    public PHASE getPhase() {
        return phase;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public CardGame setPlayerList(List<CardPlayer> playerList) {
        this.playerList = playerList;
        return this;
    }

    public CardGame setCardList(LinkedList<Card> cardList) {
        this.cardList = cardList;
        return this;
    }

    public CardGame setDiscardCardList(LinkedList<Card> discardCardList) {
        this.discardCardList = discardCardList;
        return this;
    }

    public CardGame setActQueue(List<CardAction> actQueue) {
        this.actQueue = actQueue;
        return this;
    }

    public CardGame setPhase(PHASE phase) {
        this.phase = phase;
        return this;
    }

    public CardGame setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        return this;
    }

    public CardGame setAliveNum(int aliveNum) {
        this.aliveNum = aliveNum;
        return this;
    }

    public CardGame setCurPlayerIndex(int curPlayerIndex) {
        this.curPlayerIndex = curPlayerIndex;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(to_string_basic()+"\n");
        sb.append(to_string_all_player()+"\n");
        sb.append(to_string_card_list()+"\n");
        sb.append(to_string_act_queue()+"\n");
        return sb.toString();
    }

    public String to_string_basic() {
        StringBuilder sb = new StringBuilder("");
        sb.append("[over:"+this.gameOver+" ");
        sb.append("alive:"+this.aliveNum+" ");
        sb.append("curPlayerIndex:"+this.curPlayerIndex+" ");
        sb.append("phase:"+this.phase+"]");
        return sb.toString();
    }

    public String to_string_hand_cards() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<playerList.size();i++) {
            sb.append("Player["+i+"]:\n");
            List<Card> cards = playerList.get(i).handCard;
            for (int j=0;j<cards.size();j++) {
                sb.append(cards.get(j).getPoint() +" "+cards.get(j).getClass()+"\n");
            }
        }
        return sb.toString();
    }

    public String to_string_player(int index) {
        return "Player["+index+"]"+playerList.get(index);
    }

    public String to_string_all_player() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<playerList.size();i++) {
            sb.append(to_string_player(i)+"\n");
        }
        return sb.toString();
    }

    public String to_string_card_list() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<cardList.size();i++) {
            sb.append("Card["+i+"]"+cardList.get(i).getPoint()+" "+cardList.get(i).getClass()+"\n");
        }
        for (int i=0;i<discardCardList.size();i++) {
            sb.append("DisC["+i+"]"+discardCardList.get(i).getPoint()+" "+discardCardList.get(i).getClass()+"\n");
        }
        return sb.toString();
    }

    public String to_string_act_queue() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<actQueue.size();i++) {
            sb.append("Action["+i+"]"+actQueue.get(i).type+" "+actQueue.get(i).getClass()+"\n");
        }
        return sb.toString();
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        return null;
    }

    @Override
    public void fromJson(JSONObject jobj)
        throws IOException, JSONException {

    }

    @Override
    public Action foo(Action action)
        throws Action.UnknownActionException, GameException,
        Card.SanityException, IOException {
        return action;
    }

    @Override
    public Player foo(Player player)
        throws Action.UnknownActionException, GameException,
        Card.SanityException, IOException {
        return player;
    }
}
