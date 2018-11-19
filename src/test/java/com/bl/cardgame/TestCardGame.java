package com.bl.cardgame;

import com.bl.cardgame.cards.Card1;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class TestCardGame {
  public static CardGame getCardGame() {
    List<CardPlayer> playerList = new ArrayList<CardPlayer>();
    List<Card> cards = new ArrayList<Card>();
    for (int i=0;i<10;i++) {
      cards.add(new Card1(i));
    }
    for (int j=0;j<3;j++) {
      playerList.add(new CardPlayer(j, "p"+j));
    }
    CardGame cgame = new CardGame(playerList, cards);
    return cgame;
  }

  @Test
  public void testDrawCard() throws Exception {
    List<CardPlayer> playerList = new ArrayList<CardPlayer>();
    for (int i=0;i<3;i++) {
      playerList.add(new CardPlayer(i, "p"+i));
    }
    List<Card> cards = new ArrayList<Card>();
    for (int i=0;i<10;i++) {
      cards.add(new Card1(i));
    }
    CardGame cgame = new CardGame(playerList, cards);
    try {
      cgame.playCard(new PlayCardAction(CardAction.TYPE.NON_EFFECT, 0, 0, 0, new int[]{0}));
      assertTrue("Shouldn't get here!", false);
    } catch (CardGame.GameException e) {
      assertTrue(e.getMessage().contains("Wrong Phase"));
    }
    try {
      cgame.drawCard(new DrawCardAction(1));
    } catch (CardGame.GameException e) {
      assertTrue(e.getMessage().contains("Not your turn!"));
    }
    try {
      Card card = cgame.getCardList().get(0);
      CardGame cardGame = cgame.drawCard(new DrawCardAction(0));
      assertTrue(cardGame.getPlayer(0).handCard.size() == 2);
      assertTrue(cardGame.getCardList().size() == 6);
      assertTrue(cardGame.getPlayer(0).handCard.get(1).equals(card));
    } catch (CardGame.GameException e) {
      assertTrue("Shouldn't get here!", false);
    }
    try {
      cgame.drawCard(new DrawCardAction(1));
      assertTrue("Shouldn't get here!", false);
    } catch (CardGame.GameException e) {
      assertTrue(e.getMessage().contains("Not your turn!"));
    }
  }

  @Test
  public void testPlayCardNonEffect() throws Exception {
    List<CardPlayer> playerList = new ArrayList<CardPlayer>();
    for (int i=0;i<3;i++) {
      playerList.add(new CardPlayer(i, "p"+i));
    }
    List<Card> cards = new ArrayList<Card>();
    for (int i=0;i<10;i++) {
      cards.add(new Card1(i));
    }
    CardGame cardGame = new CardGame(playerList, cards);
    cardGame.drawCard(new DrawCardAction(0));
    Card card = cardGame.getPlayer(0).handCard.get(1);
    assertTrue(cardGame.getPlayer(0).handCard.size() == 2);
    cardGame.playCard(new PlayCardAction(CardAction.TYPE.NON_EFFECT, 0, 0, 0, new int[]{}));
    assertTrue(cardGame.getPlayer(0).handCard.size() == 1);
    assertTrue(cardGame.getPlayer(0).handCard.get(0).equals(card));
  }

  @Test
  public void testCard1() throws Exception {
    List<CardPlayer> playerList = new ArrayList<CardPlayer>();
    for (int i=0;i<3;i++) {
      playerList.add(new CardPlayer(i, "p"+i));
    }
    List<Card> cards = new ArrayList<Card>();
    for (int i=0;i<4;i++) {
      cards.add(new Card1(i));
    }
    CardGame cardGame = new CardGame(playerList, cards);
    cardGame.drawCard(new DrawCardAction(0));
    Card sCard = cardGame.getPlayer(0).handCard.get(1);
    Card dCard = cardGame.getPlayer(1).handCard.get(0);
    cardGame.playCard(new PlayCardAction(CardAction.TYPE.EFFECT, 0, 0, 0, new int[]{1}));
    assertTrue(cardGame.getPlayer(0).handCard.get(0).equals(dCard));
    assertTrue(cardGame.getPlayer(1).handCard.get(0).equals(sCard));
  }

  @Test
  public void testSkill() throws Exception {
    List<CardPlayer> playerList = new ArrayList<CardPlayer>();
    for (int i=0;i<3;i++) {
      playerList.add(new CardPlayer(i, "p"+i));
    }
    List<Card> cards = new ArrayList<Card>();
    for (int i=0;i<10;i++) {
      cards.add(new Card1(i));
    }
    CardGame cardGame = new CardGame(playerList, cards);
    DrawCardAction dca = new DrawCardAction(0);
    cardGame.drawCard(dca);
    Card sCard = cardGame.getPlayer(0).handCard.get(1);
    Card dCard = cardGame.getPlayer(1).handCard.get(0);
    PlayCardAction pca = new PlayCardAction(CardAction.TYPE.NON_EFFECT, 0, 0, 0, new int[]{});
    cardGame.playCard(pca);
    SkillAction sa = new SkillAction(CardAction.TYPE.EFFECT, 0, 0, new int[] {1});
    cardGame.skill(sa);
    if (sCard.point < dCard.point) {
      assertTrue(!cardGame.getPlayer(0).isAlive());
      assertTrue(cardGame.getPlayer(1).isAlive());
    } else {
      assertTrue(cardGame.getPlayer(0).isAlive());
      assertTrue(!cardGame.getPlayer(1).isAlive());
    }
    assertTrue(cardGame.getActQueue().get(playerList.size()+1).equals(dca));
    assertTrue(cardGame.getActQueue().get(playerList.size()+2).equals(pca));
    assertTrue(cardGame.getActQueue().get(playerList.size()+3).equals(sa));
    PlayerDeadAction pda = ((PlayerDeadAction)cardGame.getActQueue()
        .get(playerList.size()+4));
    assertTrue((sCard.point<dCard.point?0:1) == pda.playerIndex);
  }
}
