package com.bl.ipc.jason;

import com.bl.ipc.BlockingClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;

public class CanaryProtoClientSideInvoker implements BlockingClient.Invoker {
  @Override public byte[] requestSerializatioin(Class protocol, Method method, Object[] args)
      throws IOException {
    try {
      String jsonStr = null;
      switch (method.getName()) {
      case "isAlive":
        jsonStr =
            "{" + "protocol:" + protocol.getName() + ","
                + "method:" + method.getName() + "}";
        return new JSONObject(jsonStr).toString().getBytes();
      case "echo":
        jsonStr =
            "{" + "protocol:" + protocol.getName() + ","
                + "method:" + method.getName() + ","
                + "str:" + args[0] + "}";
        return new JSONObject(jsonStr).toString().getBytes();
      default:
        throw new IOException("Unknown method:" + method);
      }
    } catch (JSONException e) {
      throw new IOException("got json exception ", e);
    }
  }

  @Override public Object responseDeserialization(byte[] response)
      throws IOException {
    try {
      String jsonStr = new String(response);
      JSONObject jobj = new JSONObject(jsonStr);
      String method = jobj.getString("method");
      switch (method) {
      case "isAlive":
        return jobj.getBoolean("value");
      case "echo":
        return jobj.getString("value");
      default:
        throw new IOException("Unknown method:" + method);
      }
    } catch (JSONException e) {
      throw new IOException("got json exception", e);
    }
  }
}
