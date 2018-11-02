package com.bl.ipc.jason;

import com.bl.ipc.BlockingClient;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * Client简单，只需要做"请求的序列化"和"响应的反序列化"
 * */
public class JsonRpcClient {

  /**
   * Construct a proxy to interact with Server. If it's the first time connecting
   * to Server, it will create a Daemon thread waiting for responses.
   * */
  public static Object createProtocolImpl(Class protocol,
      InetSocketAddress address, long timeout) throws IOException {
    BlockingClient.BlockingRpcInvoker invoker = new BlockingClient.BlockingRpcInvoker(protocol, address, timeout);
    return Proxy.newProxyInstance(protocol.getClassLoader(),
        new Class[] { protocol }, invoker);
  }
}
