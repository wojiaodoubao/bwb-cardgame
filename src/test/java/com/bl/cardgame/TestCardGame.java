package com.bl.cardgame;

import com.bl.cardgame.cards.Card1;

import java.util.ArrayList;
import java.util.List;

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
}
