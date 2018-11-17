package com.bl.ipc;

import com.bl.ipc.jason.JsonRpcClient;
import com.bl.ipc.jason.JsonRpcServer;
import com.bl.ipc.jason.JsonServerSideInvoker;
import com.bl.ipc.proto.CanaryProtocol;
import org.apache.log4j.BasicConfigurator;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TestIpc {
  InetSocketAddress address = new InetSocketAddress("localhost", 9080);

  @Before
  public void setup() {
    BasicConfigurator.configure();
  }

  @Test
  public void testClientRpcTimeout() throws Exception {
    JsonRpcServer server = null;
    try {
      server = new JsonRpcServer(address, 1);
      CanaryProtocol timeoutImpl = new CanaryProtocol() {
        @Override public boolean isAlive() throws IOException {
          try {
            Thread.sleep(1000 * 10);
          } catch (InterruptedException e) {
          }
          return true;
        }

        @Override public String echo(String str) throws IOException {
          return null;
        }
      };
      JsonRpcServer.map.put(CanaryProtocol.class,
          new JsonServerSideInvoker(timeoutImpl));
      server.start();

      CanaryProtocol canaryProtocol = (CanaryProtocol) JsonRpcClient
          .createProtocolImpl(CanaryProtocol.class, address, 3000);
      try {
        canaryProtocol.isAlive();
        assert false;
      } catch (IOException e) {
        String msg = e.getMessage();
        Assert.assertTrue(
            msg.contains("Timeout: 3000 ms passed waiting for response"));
      }
    } finally {
      if (server != null) {
        server.stop();
      }
      BlockingClient.closeDaemonConnectionToServer(address);
    }
  }

  @Test
  public void testClientHandleConnectionClosed() throws Exception {
    try {
      new Thread(new Runnable() {
        @Override public void run() {
          try {
            ServerSocket ss = new ServerSocket(address.getPort());
            Socket s = ss.accept();
            Thread.sleep(1000);
            s.close();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();

      CanaryProtocol canaryProtocol = (CanaryProtocol) JsonRpcClient
          .createProtocolImpl(CanaryProtocol.class, address, 3000);
      try {
        canaryProtocol.isAlive();
        assert false;
      } catch (IOException ioe) {
        String msg = ioe.getMessage();
        Assert.assertTrue(msg.contains("Connection been closed"));
      }
    } finally {
      BlockingClient.closeDaemonConnectionToServer(address);
    }
  }

  @Test
  public void testServerHandleConnectionClosed() throws Exception {
    JsonRpcServer server = null;
    try {
      server = new JsonRpcServer(address, 1);
      server.start();
      // start a bad connection, then Server will got a ClosedChannelException
      // if Exception is handled right, Server is able to serve next requests.
      // TODO: Test whether can client get server side exception.
      Socket socket = new Socket(address.getHostName(), address.getPort());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      DataInputStream in = new DataInputStream(socket.getInputStream());
      String jsonRequestStr = "{"
          + "protocol:"+CanaryProtocol.class.getName()+","
          + "method:isAlive"
          + "}";
      JSONObject jobj = new JSONObject(jsonRequestStr);
      byte[] content = jobj.toString().getBytes();
      out.writeInt(10);
      out.writeInt(content.length);
      out.write(content);
      socket.close();

      CanaryProtocol canaryProtocol = (CanaryProtocol) JsonRpcClient
          .createProtocolImpl(CanaryProtocol.class, address, 3000);
      for (int i = 0; i < 100; i++) {
        canaryProtocol.isAlive();
      }
    } finally {
      if (server != null) {
        server.stop();
      }
      BlockingClient.closeDaemonConnectionToServer(address);
    }
  }

  @Test
  public void testRpc() throws Exception {
    JsonRpcServer server = null;
    try {
      server = new JsonRpcServer(address, 1);
      server.start();
      CanaryProtocol canaryProtocol = (CanaryProtocol) JsonRpcClient
          .createProtocolImpl(CanaryProtocol.class, address, 3000);
      for (int i = 0; i < 10; i++) {
        canaryProtocol.echo("abc");
      }
    } finally {
      if (server != null) {
        server.stop();
      }
      BlockingClient.closeDaemonConnectionToServer(address);
    }
  }
}
