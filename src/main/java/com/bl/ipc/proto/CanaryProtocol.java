package com.bl.ipc.proto;

import java.io.IOException;

public interface CanaryProtocol {
  boolean isAlive() throws IOException;
  String echo(String str) throws IOException;
}