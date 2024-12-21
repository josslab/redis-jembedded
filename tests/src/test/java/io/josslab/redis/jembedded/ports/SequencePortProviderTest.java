package io.josslab.redis.jembedded.ports;

import org.junit.jupiter.api.Test;
import io.josslab.redis.jembedded.core.PortProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.josslab.redis.jembedded.core.PortProvider.newSequencePortProvider;

class SequencePortProviderTest {

  @Test
  void nextShouldIncrementPorts() {
    final int startPort = 10;
    final int portCount = 101;
    final PortProvider provider = newSequencePortProvider(startPort);

    int max = 0;
    for (int i = 0; i < portCount; i++) {
      int port = provider.get();
      if (port > max) {
        max = port;
      }
    }

    assertEquals(portCount + startPort - 1, max);
  }

}
