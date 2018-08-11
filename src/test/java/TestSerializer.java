import com.bl.cardgame.*;
import com.bl.cardgame.cards.Card1;
import com.bl.serialization.Serializer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.bl.cardgame.CardGameUtil.ComparatorUtil.*;
import static org.junit.Assert.assertTrue;
public class TestSerializer {
    @Test
    public void testJsonPlayCardActionSerialize() throws Exception {
        CardAction.TYPE type = CardAction.TYPE.NON_EFFECT;
        int sourcePlayerIndex = 0;
        int cardIndex = 1;
        int source = 0;
        int[] targets = new int[]{2};
        PlayCardAction playCardAction = new PlayCardAction(type, sourcePlayerIndex, cardIndex, source, targets);
        PlayCardAction dpaction = (PlayCardAction) Serializer.jsonToAction(Serializer.actionToJson(playCardAction));
        assertTrue(isCardActionTheSame(dpaction, playCardAction));
    }
    @Test
    public void testJsonSkillActionSerialize() throws Exception {
        SkillAction skillAction0 = new SkillAction(CardAction.TYPE.EFFECT,0,0,new int[]{1});
        SkillAction skillAction1 = (SkillAction) Serializer.jsonToAction(Serializer.actionToJson(skillAction0));
        isCardActionTheSame(skillAction0, skillAction1);
    }
    @Test
    public void testJsonGetCardGameAction() throws Exception {
        GetCardGameAction getCardGameAction0 = new GetCardGameAction();
        GetCardGameAction getCardGameAction1 = (GetCardGameAction) Serializer.jsonToAction(Serializer.actionToJson(getCardGameAction0));
        isCardActionTheSame(getCardGameAction0, getCardGameAction1);
    }
    @Test
    public void testJsonCardGameSerialize() throws Exception {
        CardGame cardGame = new CardGame();
        setCardGame(cardGame);
        CardGame seGame = (CardGame) Serializer.jsonToGame(Serializer.gameToJson(cardGame));
        assertTrue(seGame.getPhase() == cardGame.getPhase());
        assertTrue(seGame.getGameOver() == cardGame.getGameOver());
        assertTrue(seGame.getAliveNum() == cardGame.getAliveNum());
        assertTrue(seGame.getCurPlayerIndex() == cardGame.getCurPlayerIndex());
        assertTrue(isCardPlayerListTheSame(seGame.getPlayerList(), cardGame.getPlayerList()));
        assertTrue(isCardListTheSame(seGame.getCardList(), cardGame.getCardList()));
        assertTrue(isCardListTheSame(seGame.getDiscardCardList(), cardGame.getDiscardCardList()));
        assertTrue(isCardActionListTheSame(seGame.getActQueue(), cardGame.getActQueue()));
    }
    public static void setCardGame(CardGame cardGame) {
        CardGame.PHASE phase = CardGame.PHASE.START_PHASE;
        boolean gameOver = false;
        int aliveNum = 3;
        int curPlayerIndex = 0;
        PlayCardAction playCardAction = new PlayCardAction(CardAction.TYPE.NON_EFFECT,
                0, 1, 0, new int[]{2});
        List<CardAction> cardActionList = new ArrayList<CardAction>();
        cardActionList.add(playCardAction);
        List<Card> cards = new ArrayList<Card>();
        for (int i=0;i<10;i++) {
            cards.add(new Card1(i));
        }
        List<Feature> features = new ArrayList<Feature>();
        features.add(new DisableSkillFeature());
        CardPlayer player = new CardPlayer(0,"test-player-0")
                .setHandCards(cards).setFeatures(features);
        List<CardPlayer> playerList = new ArrayList<CardPlayer>();
        playerList.add(player);
        cardGame.setPhase(phase).setGameOver(gameOver).setAliveNum(aliveNum)
                .setCurPlayerIndex(curPlayerIndex).setPlayerList(playerList)
                .setCardList(new LinkedList<Card>(cards))
                .setDiscardCardList(new LinkedList<Card>(cards))
                .setActQueue(cardActionList);
    }
}
