package com.bl.cardgame;

import com.bl.ipc.jason.JsonWritable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class Feature implements JsonWritable {
  @Override public JSONObject toJson() throws IOException, JSONException {
    return new JSONObject();
  }

  @Override public void fromJson(JSONObject jobj)
      throws IOException, JSONException {

  }
}
