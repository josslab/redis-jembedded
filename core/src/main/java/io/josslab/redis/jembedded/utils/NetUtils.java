package io.josslab.redis.jembedded.utils;

import java.io.IOException;
import java.net.ServerSocket;

public final class NetUtils {

  private NetUtils() {
    // ignored
  }

  public static synchronized int allocatePort(int port) {
    if (port == 0) {
      try (ServerSocket serverSocket = new ServerSocket(0)) {
        serverSocket.setReuseAddress(false);
        return serverSocket.getLocalPort();
      } catch (IOException e) {
        return ExceptionHandler.sneakyThrow(e);
      }
    }
    return port;
  }
}
