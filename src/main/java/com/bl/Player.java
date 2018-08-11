package com.bl;

public class Player {
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
}
