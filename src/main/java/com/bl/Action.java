package com.bl;

import com.bl.ipc.jason.JsonWritable;

public abstract class Action implements JsonWritable {
    static public class UnknownActionException extends Exception {
    }
}