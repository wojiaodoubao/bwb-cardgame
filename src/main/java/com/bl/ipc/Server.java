package com.bl.ipc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * Implement 3 methods to create an RPC Server.
 * */
public abstract class Server {
  public static final Log LOG = LogFactory.getLog(Server.class);
  private final InetSocketAddress inetSocketAddress;
  private boolean running;
  private Thread listener;
  private Thread reader;
  private List<Thread> handlers = new ArrayList<>();
  private LinkedList<Connection> conQueue = new LinkedList<>();
  private LinkedList<InnerRequestQueueElem> requestQueue = new LinkedList<>();
  private final Selector readSelector;

  public static interface Invoker {
    public Response call(Request request) throws Exception;
  }

  public class Handler implements Runnable {

    @Override public void run() {
      while (running) {
        InnerRequestQueueElem rqe = null;
        try {
          synchronized (requestQueue) {
            while (requestQueue.size() == 0) {
              requestQueue.wait(30000);
            }
            rqe = requestQueue.pop();
          }
          // deserialization
          Request request = deserializeRequest(rqe.buffer);
          Invoker invoker = getProtocolMap().get(request.protocolClass);
          Response response = invoker.call(request);
          response.setConnection(rqe.connection);
          response.setCallId(rqe.callId);
          synchronized (response.con.responseQueue) {
            response.con.responseQueue.addLast(response);
          }
        } catch (InterruptedException e) {
        } catch (Exception e) {
          LOG.warn("got exception", e);
        }
      }
    }
  }

  public class Listener implements Runnable {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    public Listener() throws IOException {
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.socket().bind(inetSocketAddress);
      selector = Selector.open();
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    @Override public void run() {
      while (running) {
        try {
          selector.select();
          Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
          while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();
            Connection con = new Connection(channel);
            synchronized (conQueue) {
              conQueue.addLast(con);
              readSelector.wakeup();// so Reader won't be blocked
            }
          }
        } catch (IOException e) {
          LOG.warn("Exception while listen", e);
        }
      }
      if (serverSocketChannel != null) {
        ServerSocketChannel tmp = serverSocketChannel;
        serverSocketChannel = null;
        try {
          tmp.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public class Reader implements Runnable {

    public Reader() throws IOException {
    }

    @Override public void run() {
      while (running) {
        SelectionKey curKey = null;
        try {
          Connection con = null;
          synchronized (conQueue) {
            while (conQueue.size() > 0) {
              con = conQueue.pop();
              // attach connection to SelectionKey
              con.channel.register(readSelector,
                  SelectionKey.OP_READ | SelectionKey.OP_WRITE, con);
            }
          }
          readSelector.select(1000);
          Iterator<SelectionKey> iterator =
              readSelector.selectedKeys().iterator();
          while (iterator.hasNext()) {
            curKey = iterator.next();
            iterator.remove();
            if (curKey.isValid() && curKey.isReadable()) {
              Connection conn = (Connection) curKey.attachment();
              conn.handleRead();
            } else if (curKey.isValid() && curKey.isWritable()) {
              Connection conn = (Connection) curKey.attachment();
              conn.handleWrite();
            }
          }
        } catch (ClosedChannelException e) {
          LOG.warn("channel is closed", e);
          Connection conn = (Connection) curKey.attachment();
          conn.close();
          curKey.cancel();// cancel the registration
        } catch (IOException e) {
          LOG.warn("reader thread got exception", e);
          // TODO: we just simply close connection, to handle it better,
          // we should return this exception msg to client
          Connection conn = (Connection) curKey.attachment();
          conn.close();
          curKey.cancel();// cancel the registration
        }
      }
    }
  }

  public class Connection {
    SocketChannel channel;
    Reader reader;
    Writer writer;
    LinkedList<Response> responseQueue = new LinkedList<>();
    Connection con = this;
    class Reader {
      int dataLen = -1;
      int callId = -1;
      ByteBuffer callIdBuf = ByteBuffer.allocate(4);
      ByteBuffer dataLenBuf = ByteBuffer.allocate(4);
      ByteBuffer buffer;// TODO: optimization of buffer, it's a little waste now
      public void handleRead() throws IOException {
        // read callId
        if (callId < 0 && callIdBuf.remaining() > 0) {
          int read = channel.read(callIdBuf);
          if (read < 0) {
            throw new ClosedChannelException();
          } else if (callIdBuf.remaining() > 0) {
            return;
          }
        }
        if (callId < 0) {
          callIdBuf.flip();
          callId = callIdBuf.getInt();
          LOG.debug("read callId:" + callId);
        }
        // read dataLen
        if (dataLen < 0 && dataLenBuf.remaining() > 0) {
          int read = channel.read(dataLenBuf);
          if (read < 0) {
            throw new ClosedChannelException();
          } else if (dataLenBuf.remaining() > 0) {
            return;
          }
        }
        if (dataLen < 0) {
          dataLenBuf.flip();
          dataLen = dataLenBuf.getInt();
          LOG.debug("read dataLen:" + dataLen);
        }
        // read buffer
        if (buffer == null) {
          buffer = ByteBuffer.allocate(dataLen);
        }
        if (buffer.remaining() > 0) {
          int read = channel.read(buffer);
          if (read < 0) {
            throw new ClosedChannelException();
          } else if (buffer.remaining() > 0) {
            return;
          }
        }
        // process read content
        buffer.flip();
        synchronized (requestQueue) {
          requestQueue.addLast(new InnerRequestQueueElem(buffer, con, callId));
          requestQueue.notifyAll();
          LOG.debug("read buffer size:" + buffer.remaining());
        }
        // cleanup for next request
        callId = -1;
        dataLen = -1;
        buffer = null;
        callIdBuf.clear();
        dataLenBuf.clear();
        buffer = null;
      }
    }
    class Writer {
      Connection con = null;
      ByteBuffer buffer = null;
      ByteBuffer dataLenBuf = ByteBuffer.allocate(4);
      ByteBuffer callIdBuf = ByteBuffer.allocate(4);
      public void handleWrite() throws IOException {
        if (buffer == null) {
          Response response = null;
          synchronized (responseQueue) {
            if (responseQueue.size() == 0) {
              return;
            } else {
              response = responseQueue.pop();
            }
          }
          buffer = serializeResponse(response);
          assert dataLenBuf.remaining() == 4;
          dataLenBuf.putInt(buffer.remaining());
          dataLenBuf.flip();
          assert callIdBuf.remaining() == 4;
          callIdBuf.putInt(response.callId);
          callIdBuf.flip();
        }
        // send callId back
        if (callIdBuf.remaining() > 0) {
          int len = channel.write(callIdBuf);
          if (len < 0) {
            throw new ClosedChannelException();
          } else if (callIdBuf.remaining() > 0) {
            return;
          }
        }
        // send dataLenBuf back
        if (dataLenBuf.remaining() > 0) {
          int len = channel.write(dataLenBuf);
          if (len < 0) {
            throw new ClosedChannelException();
          } else if (dataLenBuf.remaining() > 0) {
            return;
          }
        }
        // send response back
        if (buffer.remaining() > 0) {
          int len = channel.write(buffer);
          if (len < 0) {
            throw new ClosedChannelException();
          } else if (buffer.remaining() > 0) {
            return;
          }
        }
        // done
        buffer = null;
        con = null;
        dataLenBuf.clear();
        callIdBuf.clear();
        buffer = null;
      }
    }
    public Connection(SocketChannel channel) throws IOException {
      this.channel = channel;
      channel.configureBlocking(false);// non-blocking mode
      reader = new Reader();
      writer = new Writer();
    }
    public void handleRead() throws IOException {
      reader.handleRead();
    }
    public void handleWrite() throws IOException {
      writer.handleWrite();
    }
    public void close() {
      try {
        channel.close();
      } catch (IOException e) {
      }
    }
  }

  public static abstract class Request<T> {
    protected Class protocolClass;
    public Class getProtocolClass() {
      return protocolClass;
    }
  }

  public static abstract class Response<T> {
    Connection con;
    int callId;
    public void setConnection(Connection con) {
      this.con = con;
    }
    public void setCallId(int callId) {
      this.callId = callId;
    }
  }

  private static class InnerRequestQueueElem {
    ByteBuffer buffer;
    Connection connection;
    int callId;

    public InnerRequestQueueElem(ByteBuffer buffer, Connection con,
        int callId) {
      this.buffer = buffer;
      this.connection = con;
      this.callId = callId;
    }
  }

  /**
   * Serialize response to ByteBuffer, ByteBuffer should in read mode.
   * */
  abstract public ByteBuffer serializeResponse(Response response)
      throws IOException;
  /**
   * Deserialize request from ByteBuffer, ByteBuffer is in read mode.
   * */
  abstract public Request deserializeRequest(ByteBuffer buffer)
      throws IOException;
  /**
   * Map protocol Class to corresponding Invoker.
   * */
  abstract public Map<Class, Invoker> getProtocolMap();

  public Server(InetSocketAddress inetSocketAddress, int handlerCount)
      throws IOException {
    this.inetSocketAddress = inetSocketAddress;
    this.listener = new Thread(new Listener());
    this.reader = new Thread(new Reader());
    for (int i = 0; i < handlerCount; i++) {
      Thread t = new Thread(new Handler());
      handlers.add(t);
    }
    this.readSelector = Selector.open();
  }

  public void start() {
    running = true;
    listener.start();
    reader.start();
    for (Thread t : handlers) {
      t.start();
    }
  }

  public void stop() {
    // listener should stop as fast as possible.
    // handler should try to finish current request, but shouldn't generate any response
    // reader should try to finish cached responses in a limit time.
    this.running = false;
    while (listener.isAlive()) {
      listener.interrupt();
      try {
        listener.join();
      } catch (InterruptedException e) {
      }
    }
    for (Thread t : handlers) {
      t.interrupt();
    }
    for (Thread t : handlers) {
      while (t.isAlive()) {
        try {
          t.join();
        } catch (InterruptedException e) {
        }
      }
    }
    reader.interrupt();
    while (reader.isAlive()) {
      try {
        reader.join();
      } catch (InterruptedException e) {
      }
    }
  }

  public boolean isRunning() {
    return running;
  }
}
