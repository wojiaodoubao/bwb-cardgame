package com.bl.cardgame;

import java.util.Comparator;
import java.util.List;

public class CardGameUtil {
    public static class ComparatorUtil {
        public static <T> boolean isListTheSame(List<T> list1, List<T> list2, Comparator<T> comparator) {
            if (list1 == list2) return true;
            if (list1 != null && list2 != null) {
                if (list1.size() != list2.size()) return false;
                for (int i=0;i<list2.size();i++) {
                    if (0 != comparator.compare(list1.get(i),list2.get(i))) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public static boolean isCardActionTheSame(CardAction action1, CardAction action2) {
            if (action1 == action2) return true;
            // TODO: 比较cardAction & cardActionList写了，之后给第一个单测改写了
            if (action1 != null && action2 != null) {
                if (action1.getClass() != action2.getClass()) return false;
                if (action1.type != action2.type) return false;
                if (action1.timestamp != action2.timestamp) return false;
                if (action1.srcPlayerIndex != action2.srcPlayerIndex) return false;
                if (action1 instanceof PlayerDeadAction) {
                    PlayerDeadAction act1 = (PlayerDeadAction) action1;
                    PlayerDeadAction act2 = (PlayerDeadAction) action2;
                    if (act1.playerIndex != act2.playerIndex) return false;
                    if (!act1.msg.equals(act2.msg)) return false;
                } else if (action1 instanceof DrawCardAction) {
                    ;
                } else if (action1 instanceof SkillAction) {
                    ;
                } else if (action1 instanceof PlayCardAction) {
                    PlayCardAction act1 = (PlayCardAction) action1;
                    PlayCardAction act2 = (PlayCardAction) action2;
                    if (act1.cardIndex != act2.cardIndex) return false;
                    if (act1.source != act2.source) return false;
                    if (act1.targets.length != act2.targets.length) return false;
                    for (int i=0;i<act1.targets.length;i++) {
                        if (act1.targets[i] != act2.targets[i]) return false;
                    }
                } else if (action1 instanceof GetCardGameAction) {
                    ;
                } else if (action1 instanceof ExchangeCardAction) {
                    ExchangeCardAction act1 = (ExchangeCardAction) action1;
                    ExchangeCardAction act2 = (ExchangeCardAction) action2;
                    if (act1.srcPid != act2.srcPid) return false;
                    if (act1.dstPid != act2.dstPid) return false;
                }
                return true;
            }
            return false;
        }

        public static boolean isCardActionListTheSame(List<CardAction> list1, List<CardAction> list2) {
            return isListTheSame(list1, list2, new Comparator<CardAction>() {
                @Override
                public int compare(CardAction o1, CardAction o2) {
                    return isCardActionTheSame(o1, o2) ? 0 : 1;
                }
            });
        }

        public static boolean isCardTheSame(Card card1,Card card2) {
            if (card1 == card2) return true;
            if (card1.getClass() != card2.getClass()) return false;
            if (card1.getPoint() != card2.getPoint()) return false;
            return true;
        }

        public static boolean isCardListTheSame(List<Card> list1, List<Card> list2) {
            return isListTheSame(list1, list2, new Comparator<Card>() {
                @Override
                public int compare(Card o1, Card o2) {
                    return isCardTheSame(o1, o2) ? 0 : 1;
                }
            });
        }

        public static boolean isFeatureTheSame(Feature feature1, Feature feature2) {
            if (feature1 == feature2) return true;
            if (feature1.getClass() != feature2.getClass()) return false;
            return true;
        }

        public static boolean isFeatureListTheSame(List<Feature> features1, List<Feature> features2) {
            return isListTheSame(features1, features2, new Comparator<Feature>() {
                @Override
                public int compare(Feature o1, Feature o2) {
                    return isFeatureTheSame(o1, o2) ? 0 : 1;
                }
            });
        }

        public static boolean isCardPlayerTheSame(CardPlayer player1, CardPlayer player2) {
            if (player1  == player2) {
                return true;
            }
            if (player1 != null && player2 != null) {
                if (player1.getId() != player2.getId()) return false;
                if (!player1.getName().equals(player2.getName())) return false;
                if (player1.isAlive() != player2.isAlive()) return false;
                if (!isCardListTheSame(player1.getHandCards(),player2.getHandCards())) return false;
                if (!isFeatureListTheSame(player1.getFeatures(), player2.getFeatures())) return false;
                return true;
            }
            return false;
        }

        public static boolean isCardPlayerListTheSame(List<CardPlayer> list1, List<CardPlayer> list2) {
            return isListTheSame(list1, list2, new Comparator<CardPlayer>() {
                @Override
                public int compare(CardPlayer o1, CardPlayer o2) {
                    return isCardPlayerTheSame(o1, o2) ? 0 : 1;
                }
            });
        }
    }
}
