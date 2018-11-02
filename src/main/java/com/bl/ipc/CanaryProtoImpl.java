package com.bl.ipc;

import com.bl.ipc.proto.CanaryProtocol;

import java.io.IOException;

public class CanaryProtoImpl implements CanaryProtocol {

  @Override public boolean isAlive() {
    return true;
  }

  @Override public String echo(String s) throws IOException {
    return s;
  }
}
