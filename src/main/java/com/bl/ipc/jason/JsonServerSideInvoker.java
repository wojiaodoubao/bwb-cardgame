package com.bl.ipc.jason;

import com.bl.ipc.Server;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.lang.reflect.Method;

public class JsonServerSideInvoker implements Server.Invoker {

  protected Object impl;

  public JsonServerSideInvoker(Object impl) {
    this.impl = impl;
  }

  @Override
  public Server.Response call(Server.Request request)
      throws Exception {
    JSONObject res = new JSONObject();
    JsonRpcServer.JsonRequest jrequest = (JsonRpcServer.JsonRequest) request;
    JSONObject jobj = jrequest.jobj;
    Class clazz = jrequest.getProtocolClass();
    String methodName = jobj.getString("method");

    JSONArray paramClazzArray = jobj.getJSONArray("param.clazz");
    Class[] paramTypes = new Class[paramClazzArray.length()];
    for (int i=0;i<paramTypes.length;i++) {
      paramTypes[i] = Class.forName(paramClazzArray.getString(i));
    }
    JSONArray paramArray = jobj.getJSONArray("param");
    Object[] params = new Object[paramArray.length()];
    for (int i=0;i<paramArray.length();i++) {
      Object obj = paramArray.get(i);
      if (JSONObject.class.isAssignableFrom(obj.getClass())) {// to JsonWritable
        params[i] = JsonWrapper.fromJson((JSONObject)obj);
      } else { // to basic
        params[i] = obj;
      }
    }

    Method method = getMethod(clazz, methodName, paramTypes);
    Object resultObj = method.invoke(impl, params);
    if (JsonWritable.class.isAssignableFrom(resultObj.getClass())) { // to JsonWritable
      res.put("value", JsonWrapper.toJson((JsonWritable)resultObj));
    } else { // to java basic
      res.put("value", resultObj);
    }
    return new JsonRpcServer.JsonResponse(res);
  }

  private Method getMethod(Class clazz, String methodName, Class[] paramTypes)
      throws Exception {
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      if (method.getParameterCount() == paramTypes.length &&
          method.getName().equals(methodName)) {
        if (paramTypes.length == 0) return method;
        Class[] pts = method.getParameterTypes();
        int i = 0;
        for (;i<pts.length;i++) {
          if (!pts[i].isAssignableFrom(paramTypes[i])) break;
        }
        if (i == pts.length) {
          return method;
        }
      }
    }
    String paramStr = "(";
    for (Class param : paramTypes) {
      paramStr += param.getName() + ",";
    }
    paramStr += ")";
    throw new Exception("couldn't find proper method " + methodName + paramStr);
  }
}
