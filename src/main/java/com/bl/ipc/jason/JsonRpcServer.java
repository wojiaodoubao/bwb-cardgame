package com.bl.ipc.jason;

import com.bl.ipc.CanaryProtoImpl;
import com.bl.ipc.proto.CanaryProtocol;
import com.bl.ipc.Server;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 为JsonRpc添加协议与实现
 * 1. 定义一个协议A，放在proto包下
 * 2. 实现这个协议Aimpl，放在com.bl下
 * 3. 实现一个AClientSideInvoker implements BlockingClient.Invoker，实现Client端对request和response的序列化与反序列化，注册A.class,impl给BlockingClient.map
 * 4. 实现一个AServerSideInvoker implements Server.Invoker，实现Server端对request的解析，处理，打包response，注册A.class,impl给Server.map
 * TODO：因为RpcEngine实现了完全的不用管序列化，我这里有可能实现，上层不需要管json序列化吗？但是要知道至少是要实现Writable的，
 * TODO：所以我可以让上层不管序列化的前提是不是，得让上层实现要序列化对象的Json序列化？
 * */
public class JsonRpcServer extends Server {

  public final static HashMap<Class, Invoker> map = new HashMap<>();
  static {
    CanaryProtoServerSideInvoker
        canaryProtoInvoker = new CanaryProtoServerSideInvoker(new CanaryProtoImpl());
    map.put(CanaryProtocol.class, canaryProtoInvoker);
  }

  public JsonRpcServer(InetSocketAddress inetSocketAddress, int handlerCount)
      throws IOException {
    super(inetSocketAddress, handlerCount);
  }

  @Override
  public Request deserializeRequest(ByteBuffer buffer) throws IOException {
    byte[] content = new byte[buffer.remaining()];
    buffer.get(content);
    String jsonString = new String(content);
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      JsonRequest jsonRequest = new JsonRequest(jsonObject);
      return jsonRequest;
    } catch (JSONException e) {
      throw new IOException("Couldn't deserialize in json", e);
    } catch (ClassNotFoundException e) {
      throw new IOException("Couldn't deserialize in json", e);
    }
  }

  @Override public Map<Class, Invoker> getProtocolMap() {
    return map;
  }

  @Override public ByteBuffer serializeResponse(Response response)
      throws IOException {// 返回前记得flip
    if (!(response instanceof JsonResponse)) {
      throw new IOException(
          "JsonRpcServer expects JsonResponse, but response is " + response
              .getClass());
    }
    JsonResponse jsonResponse = (JsonResponse) response;
    byte[] content = jsonResponse.jobj.toString().getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(content.length);
    buffer.put(content);
    buffer.flip();
    return buffer;
  }

  public static class JsonRequest extends Server.Request {
    JSONObject jobj;
    public JsonRequest(JSONObject jobj)
        throws JSONException, ClassNotFoundException {
      this.jobj = jobj;
      this.protocolClass = Class.forName(jobj.getString("protocol"));
    }
  }

  public static class JsonResponse extends Server.Response {
    JSONObject jobj;
    public JsonResponse(JSONObject jobj) {
      this.jobj = jobj;
    }
  }
}
