package io.josslab.redis.jembedded.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.SecureRandom;

public final class NetUtils {
  private static final int MAX_REDIS_PORT = 55535;
  private static final int MIN_REDIS_PORT = 3000;
  private static final SecureRandom RANDOM = new SecureRandom();

  private NetUtils() {
    // ignored
  }

  public static synchronized int allocatePort(int port) {
    if (port == 0) {
      while (true) {
        port = RANDOM.nextInt(MAX_REDIS_PORT - MIN_REDIS_PORT) + MIN_REDIS_PORT;
        try (ServerSocket socket = new ServerSocket(port, 1)) {
          socket.setReuseAddress(true);
          return socket.getLocalPort();
        } catch (IOException e) {
          // ignored
        }
      }
    }
    return port;
  }
}
