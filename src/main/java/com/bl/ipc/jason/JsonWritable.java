package com.bl.ipc.jason;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public interface JsonWritable {
  public JSONObject toJson() throws IOException, JSONException;
  public void fromJson(JSONObject jobj) throws IOException, JSONException;
}
