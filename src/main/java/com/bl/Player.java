package com.bl;

import com.bl.ipc.jason.JsonWritable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class Player implements JsonWritable {
    protected int id;
    protected boolean alive;
    protected String name;
    public Player() { }
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
    }
    public boolean isAlive() {
        return alive;
    }
    public String getName() {
        return this.name;
    }
    public int getId() {
        return id;
    }
    public Player setAlive(boolean alive) {
        this.alive = alive;
        return this;
    }
    public Player setName(String name) {
        this.name = name;
        return this;
    }
    public Player setId(int id) {
        this.id = id;
        return this;
    }
    @Override
    public String toString() {
        return "id:" + id + " alive:" + alive + " name:" + name;
    }

    @Override
    public JSONObject toJson() throws IOException, JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(Player.class+"id", id);
        jobj.put(Player.class+"alive", alive);
        jobj.put(Player.class+"name", name);
        return jobj;
    }

    @Override
    public void fromJson(JSONObject jobj) throws IOException, JSONException {
        id = jobj.getInt(Player.class+"id");
        alive = jobj.getBoolean(Player.class+"alive");
        name = jobj.getString(Player.class+"name");
    }
}
