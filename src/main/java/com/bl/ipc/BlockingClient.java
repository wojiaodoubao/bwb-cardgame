package com.bl.ipc;

import com.bl.ClientGameProtocol;
import com.bl.ipc.jason.JsonClientSideInvoker;
import com.bl.ipc.proto.CanaryProtocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BlockingClient runs in BlockingIO mode, keeps a long term connection to Server, and
 * has a daemon thread waiting for Server's responses.
 * */
public class BlockingClient { // BlockingIO BlockingClient / long term connection

  public static final Log LOG = LogFactory.getLog(BlockingClient.class);
  static HashMap<InetSocketAddress, BlockingClient> clientMap = new HashMap<>();

  public static interface Invoker {// handles Se & De, must be stateless
    public byte[] requestSerializatioin(Class protocol, Method method, Object[] args)
        throws IOException;
    public Object responseDeserialization(byte[] response)
        throws IOException;
  }

  public final static HashMap<Class, Invoker> map = new HashMap<>();
  static { // register Se/De Invoker to protocol
    map.put(CanaryProtocol.class, new JsonClientSideInvoker());
    map.put(ClientGameProtocol.class, new JsonClientSideInvoker());
  }

  public static BlockingClient getClient(InetSocketAddress address) throws IOException {
    synchronized (clientMap) {
      BlockingClient client = clientMap.get(address);
      if (client == null || client.closed) {
        client = new BlockingClient(address);
        clientMap.put(address, client);
      }
      return client;
    }
  }
  /**
   * Close client connecting to address.
   * */
  public static void closeDaemonConnectionToServer(InetSocketAddress address) {
    BlockingClient client = clientMap.get(address);
    if (client != null) {
      client.close(null);
    }
  }

  DataOutputStream out;
  DataInputStream in;
  Socket socket;
  InetSocketAddress address;
  boolean closed;
  IOException closeException;
  AtomicInteger callIdIncrement;
  HashMap<Integer, Response> waitResponseMap = new HashMap<>();
  Thread rr;
  class ResponseReceiver implements Runnable {
    @Override public void run() {
      try {
        while (!closed) {
          int callId = in.readInt();// DataInputStream will get an EOFException
                                    // when connection is closed
          int len = in.readInt();
          byte[] content = new byte[len];
          if (in.read(content) != len) { // read() returns either len or -1
            throw new IOException("Unexpected len.");
          }
          synchronized (waitResponseMap) {
            Response response = waitResponseMap.get(callId);
            if (response != null) {
              response.callId = callId;
              response.response = content;
              response.done = true;
            }
            if (response.callback != null) {
              // TODO: change to thread pool
              new Thread(new Runnable() {
                @Override public void run() {
                  response.callback.callback(response);
                }
              }).start();
            } else {
              waitResponseMap.notifyAll();
            }
          }
        }
      } catch (IOException e) {
        LOG.warn("Exception while waiting for response, close client", e);
        close(e);
      }
    }
  }
  public class Response { // TODO: support the response of the Server Exception
    boolean done = false;
    int callId;
    byte[] response;
    ResponseCallback callback = null;
    long curTime = System.currentTimeMillis();
  }
  interface ResponseCallback {
    void callback(Response response);
  }
  /**
   * client rpc in blocking mode
   * */
  public static class BlockingRpcInvoker implements InvocationHandler {
    protected final Class protocol;
    Invoker invoker;
    final InetSocketAddress isa;
    final long timeout;

    /**
     * Construct a BlockingRpcInvoker
     * @param protocol protocol used between BlockingClient & Server
     * @param isa Server address
     * @param timeout timeout for each rpc call, timeout<0 means waiting forever
     *                throw IOException if no response before timeout.
     * */
    public BlockingRpcInvoker(Class protocol, InetSocketAddress isa,
        long timeout) throws IOException {
      this.protocol = protocol;
      this.isa = isa;
      this.timeout = timeout;
      this.invoker = map.get(protocol);
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      byte[] request = invoker.requestSerializatioin(protocol, method, args);
      BlockingClient client = BlockingClient.getClient(isa);
      int callId = client.sendRequest(request);
      BlockingClient.Response response = client.getResponseBlocking(callId, timeout);
      return invoker.responseDeserialization(response.response);
    }
  }

  public BlockingClient(InetSocketAddress address) throws IOException {
    socket = new Socket();
    socket.connect(address, 30 * 1000);
    out = new DataOutputStream(socket.getOutputStream());
    in = new DataInputStream(socket.getInputStream());
    closed = false;
    callIdIncrement = new AtomicInteger();
    this.address = address;
    rr = new Thread(new ResponseReceiver());
    rr.setDaemon(true);
    rr.start();
  }

  public int sendRequest(byte[] request) { // blocking
    int callId = callIdIncrement.getAndIncrement();
    try {
      out.writeInt(callId);
      out.writeInt(request.length);
      out.write(request);
    } catch (IOException e) {
      close(e);
    }
    synchronized (waitResponseMap) {
      waitResponseMap.put(callId, new Response());
    }
    return callId;
  }

  public int sendRequestAsync(byte[] request, ResponseCallback callback) {
    int callId = callIdIncrement.getAndIncrement();
    synchronized (waitResponseMap) {
      Response res = new Response();
      res.callback = callback;
      waitResponseMap.put(callId, res);
    }
    try {
      out.writeInt(callId);
      out.writeInt(request.length);
      out.write(request);
    } catch (IOException e) {
      close(e);
    }
    return callId;
  }

  public Response getResponseBlocking(int callId, long timeout)
      throws InterruptedException, IOException {
    synchronized (waitResponseMap) {
      Response response = waitResponseMap.get(callId);
      if (response == null) {
        throw new IOException("Bad callId!");
      } else {
        while (!closed && !response.done) {
          if (timeout > 0
              && System.currentTimeMillis() - response.curTime > timeout) {
            throw new IOException(
                "Timeout: " + timeout + " ms passed waiting for response");
          }
          waitResponseMap.wait(timeout);
        }
        if (closed && !response.done) {
          if (closeException != null) {
            throw new IOException("Connection been closed!", closeException);
          } else {
            throw new IOException("Connection been closed!");
          }
        } else {
          return response;
        }
      }
    }
  }

  public Response getResponse(int callId) {
    synchronized (waitResponseMap) {
      Response response = waitResponseMap.get(callId);
      if (response != null && response.done) {
        return  response;
      } else {
        return null;
      }
    }
  }

  public synchronized void close(IOException e) {
    if (closed) return;
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException ioe) {
      LOG.warn("Exception while closing client", ioe);
    } finally {
      closed = true;
      closeException = e;
      synchronized (clientMap) {
        clientMap.remove(address);
      }
    }
  }
}
