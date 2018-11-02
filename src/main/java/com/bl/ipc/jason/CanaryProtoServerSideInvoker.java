package com.bl.ipc.jason;

import com.bl.ipc.proto.CanaryProtocol;
import com.bl.ipc.Server;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.bl.ipc.jason.JsonRpcServer.JsonRequest;
import com.bl.ipc.jason.JsonRpcServer.JsonResponse;

import java.io.IOException;

public class CanaryProtoServerSideInvoker implements Server.Invoker {

  CanaryProtocol impl = null;

  public CanaryProtoServerSideInvoker(CanaryProtocol impl) {
    this.impl = impl;
  }

  @Override public Server.Response call(Server.Request request)
      throws Exception {
    JsonRequest jrequest = (JsonRequest) request;
    JSONObject jobj = jrequest.jobj;
    String method = jobj.getString("method");
    if (method.equals("isAlive")) {
      boolean bool = impl.isAlive();
      return constructResult(jrequest.getProtocolClass().getName(), method, bool);
    } else if (method.equals("echo"))  {
      String str = jobj.getString("str");
      String s = impl.echo(str);
      return constructResult(jrequest.getProtocolClass().getName(), method, s);
    } else {
      throw new IOException("Bad method name:" + method);
    }
  }

  private JsonResponse constructResult(String protocol, String method, boolean value)
      throws JSONException {
    JSONObject res = new JSONObject();
    res.put("protocol", protocol);
    res.put("method", method);
    res.put("value", value);
    return new JsonResponse(res);
  }

  private JsonResponse constructResult(String protocol, String method, String value)
      throws JSONException {
    JSONObject res = new JSONObject();
    res.put("protocol", protocol);
    res.put("method", method);
    res.put("value", value);
    return new JsonResponse(res);
  }
}
