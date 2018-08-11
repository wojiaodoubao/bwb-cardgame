import com.bl.Action;
import com.bl.ClientGameProtocol;
import com.bl.Game;
import com.bl.Player;
import com.bl.cardgame.*;
import com.bl.rpc.RPCClient;
import com.bl.rpc.RPCServer;
import com.bl.serialization.Serializer;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
public class TestRpc {
    @Test
    public void testSendRpc() throws Exception {
        DrawCardAction drawCardAction = new DrawCardAction(0,0);
        PlayCardAction playCardAction = new PlayCardAction(CardAction.TYPE.NON_EFFECT,
                0, 1, 0, new int[]{2});
        SkillAction skillAction = new SkillAction(CardAction.TYPE.EFFECT,0,0,new int[]{1});
        GetCardGameAction getCardGameAction = new GetCardGameAction();
        final CardAction[] cardActions = new CardAction[] {drawCardAction, playCardAction, skillAction, getCardGameAction};
        int port = 8090;
        final AtomicInteger i = new AtomicInteger(0);
        class MockGame extends CardGame {
            @Override
            public Game doAction(Action action) throws Action.UnknownActionException, CardGame.GameException, Card.SanityException {
                assertTrue(action instanceof CardAction);
                assertTrue(CardGameUtil.ComparatorUtil.isCardActionTheSame(cardActions[i.getAndIncrement()], (CardAction)action));
                return this;
            }
            @Override
            public CardGame getGame(Player player) {
                return this;
            }
        }
        CardGame mockGame = new MockGame();
        TestSerializer.setCardGame(mockGame);
        RPCServer server = new RPCServer(mockGame, port);
        RPCClient client = new RPCClient("localhost",8090);
        server.start();
        for (CardAction cardAction : cardActions) {
            client.doAction(cardAction);
        }
        while (i.get() < cardActions.length) {
            Thread.sleep(100);
        }
        server.close();
        client.close();
    }

}
