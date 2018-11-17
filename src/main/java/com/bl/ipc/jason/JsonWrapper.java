package com.bl.ipc.jason;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

public class JsonWrapper { // TODO:JsonWrapper看起来就是之前Serializer的升级版，但是我还真没想到更好的写法；
                           // TODO:CardAction的注释我看了，但是我现在看不懂了；
                           // TODO:问题是，ClientSideInvoker在ipc client看来负责序列化，实际上我想只负责附加method信息，序列化单独由JsonWrapper控制；
                           // TODO:ServerSideInvoker在ipc Server看来负责反序列化，映射方法与处理，实际上只负责映射方法？反序列化给JsonWrapper，处理给GameServer？
                           // TODO:怎么能很好的把sede给剥离开来，怎么能很好的把映射方法给剥离开来？用反射？注册Map？是不是有一些是注定做不到的？
  public static final String JSON_CLASS = "json.obj";

  public static JSONObject toJson(JsonWritable jw)
      throws JSONException, IOException {
    JSONObject jobj = jw.toJson();
    jobj.put(JSON_CLASS, jw.getClass().getName());
    return jobj;
  }

  public static JsonWritable fromJson(JSONObject jobj)
      throws IOException, JSONException {
    String className = jobj.getString(JSON_CLASS);
    try {
      Class clazz = Class.forName(className);
      Constructor con = clazz.getDeclaredConstructor(new Class[0]);
      JsonWritable jw = (JsonWritable) con.newInstance(new Object[0]);
      jw.fromJson(jobj);
      return jw;
    } catch (Exception e) {
      throw new IOException("deserialize error", e);
    }
  }

  /**
   * Serialize obj, which class is param clazz, and all fields are basic types.
   * Take care when calling this method, if a class using this method, then all
   * subclasses shouldn't have non basic type member.
   * */
  @Deprecated
  public static void objectToJson(Class clazz, Object obj, JSONObject jobj)
      throws JSONException, IOException {
    try {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
        if (JsonWritable.class.isAssignableFrom(field.getType())) {
          jobj.put(field.getName(), toJson((JsonWritable)field.get(obj)));
        } else {
          jobj.put(field.getName(), field.get(obj));
        }
      }
    } catch (IllegalAccessException e) {
      throw new IOException("serialization error", e);
    }
  }

  /**
   * Deserialize jobj to obj, which class is param clazz,
   * and all fields are basic types.
   * Take care when calling this method, if a class using this method, then all
   * subclasses shouldn't have non basic type member.
   * */
  @Deprecated
  public static void jsonToObject(Class clazz, Object obj, JSONObject jobj)
      throws JSONException, IOException {
    try {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
        Object o = jobj.get(field.getName());
        if (JSONObject.class.isAssignableFrom(o.getClass())) {
          field.set(obj, fromJson((JSONObject) o));
        } else {
          field.set(obj, o);
        }
      }
    } catch (IllegalAccessException e) {
      throw new IOException("serialization error", e);
    }
  }

  public static JSONArray collectionToJsonArray(Collection collection, JSONArray array)
      throws IOException, JSONException {
    Iterator iterator = collection.iterator();
    if (iterator.hasNext()) {
      Object obj = iterator.next();
      if (JsonWritable.class.isAssignableFrom(obj.getClass())) {
        array.put(toJson((JsonWritable)obj));
      } else {
        array.put(obj);
      }
    }
    return array;
  }

  public static Collection jsonArrayToCollection(JSONArray array, Collection collection)
      throws JSONException, IOException {
    for (int i=0;i<array.length();i++) {
      Object obj = array.get(i);
      if (JSONObject.class.isAssignableFrom(obj.getClass())) {
        collection.add(fromJson((JSONObject)obj));
      } else {
        collection.add(obj);
      }
    }
    return collection;
  }

  public static <T> JSONArray arrayToJSONArray(T[] array, JSONArray jarray)
      throws IOException, JSONException {
    if (array.length == 0) return jarray;
    if (JsonWritable.class.isAssignableFrom(array[0].getClass())) {
      for (int i=0;i<array.length;i++) {
        jarray.put(toJson((JsonWritable)array[i]));
      }
    } else {
      for (int i=0;i<array.length;i++) {
        jarray.put(array[i]);
      }
    }
    return jarray;
  }

  public static <T> T[] jsonArrayToArray(JSONArray jarray, T[] array)
      throws JSONException, IOException {
    for (int i=0;i<jarray.length();i++) {
      Object obj = jarray.get(i);
      if (JSONObject.class.isAssignableFrom(obj.getClass())) {
        array[i] = (T) fromJson((JSONObject)obj);
      } else {
        array[i] = (T) obj;
      }
    }
    return array;
  }
}
