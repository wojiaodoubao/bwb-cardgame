package com.bl.ipc.jason;

import com.bl.ipc.BlockingClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;

public class JsonClientSideInvoker implements BlockingClient.Invoker {
  @Override public byte[] requestSerializatioin(Class protocol, Method method,
      Object[] args) throws IOException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("protocol", protocol.getName());
      jsonObject.put("method", method.getName());
      if (args == null) {
        args = new Object[0];
      }
      JSONArray paramClazzs = new JSONArray();
      JSONArray params = new JSONArray();
      for (Object arg : args) {
        if (JsonWritable.class.isAssignableFrom(arg.getClass())) {
          params.put(JsonWrapper.toJson((JsonWritable) arg));
        } else {
          params.put(arg);
        }
        paramClazzs.put(arg.getClass().getName());
      }
      jsonObject.put("param.clazz", paramClazzs);
      jsonObject.put("param", params);
    } catch (JSONException e) {
      throw new IOException("json error", e);
    }
    return jsonObject.toString().getBytes();
  }

  @Override public Object responseDeserialization(byte[] response)
      throws IOException {
    try {
      String jsonStr = new String(response);
      JSONObject jobj = new JSONObject(jsonStr);
      Object res = jobj.get("value");
      if (JSONObject.class.isAssignableFrom(res.getClass())) { // for JsonWritable objects
        return JsonWrapper.fromJson((JSONObject) res);
      } else { // for basic java types only
        return res;
      }
    } catch (JSONException e) {
      throw new IOException("json error", e);
    }
  }
}
