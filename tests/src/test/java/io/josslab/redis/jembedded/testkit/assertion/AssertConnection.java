package io.josslab.redis.jembedded.testkit.assertion;

import org.junit.platform.commons.PreconditionViolationException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.String.format;

public final class AssertConnection {

  public static final int DEFAULT_TIMEOUT = 1000; //ms

  private AssertConnection() {
    // ignored
  }

  public static void assertSocketAvailable(final String host, final int port) {
    assertSocketAvailable(host, port, DEFAULT_TIMEOUT);
  }

  public static void assertSocketAvailable(final String host, final int port, final int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
    } catch (UnknownHostException e) {
      throw new PreconditionViolationException(format(
        "Could not connect to %s:%d - %s", host, port, e.getMessage()
      ));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
