package com.bl.ipc;

import com.bl.ipc.proto.ClientGameProtocol;
import com.bl.cardgame.*;
import com.bl.ipc.jason.JsonRpcClient;
import com.bl.ipc.jason.JsonRpcServer;
import com.bl.ipc.jason.JsonServerSideInvoker;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class TestClientProtocolIPC {
  InetSocketAddress address = new InetSocketAddress("localhost", 9080);
  @Before
  public void setup() {
    BasicConfigurator.configure();
  }

  // test ClientProtocol by calling foo
  @Test
  public void testClientProtocol() throws Exception {
    JsonRpcServer server = null;
    try {
      // register test game to ipc server and start server.
      CardGame testGame = CardGame.getCardGame(3);
      JsonServerSideInvoker
          clientGameProtoInvoker = new JsonServerSideInvoker(testGame);
      JsonRpcServer.map.put(ClientGameProtocol.class, clientGameProtoInvoker);
      server = new JsonRpcServer(address, 1);
      server.start();

      ClientGameProtocol clientProtocol = (ClientGameProtocol) JsonRpcClient
          .createProtocolImpl(ClientGameProtocol.class, address, 3000);

      PlayerDeadAction playerDeadAction = new PlayerDeadAction(1, "1");
      clientProtocol.foo(playerDeadAction);
      DrawCardAction drawCardAction = new DrawCardAction(0);
      clientProtocol.foo(drawCardAction);
      ShuffleCardAction shuffleCardAction = new ShuffleCardAction();
      clientProtocol.foo(shuffleCardAction);
      ArrayList targets = new ArrayList<>();
      targets.add(0);
      targets.add(1);
      PlayCardAction playCardAction = new PlayCardAction(CardAction.TYPE.EFFECT, 1, 1, targets);
      clientProtocol.foo(playCardAction);
      SkillAction skillAction = new SkillAction();
      clientProtocol.foo(skillAction);
      GetCardGameAction getCardGameAction = new GetCardGameAction();
      clientProtocol.foo(getCardGameAction);
      ExchangeCardAction exchangeCardAction = new ExchangeCardAction(2,3);
      clientProtocol.foo(exchangeCardAction);

      clientProtocol.foo(testGame.getPlayer(1));
    } finally {
      if (server != null) {
        server.stop();
      }
      BlockingClient.closeDaemonConnectionToServer(address);
    }
  }
}
