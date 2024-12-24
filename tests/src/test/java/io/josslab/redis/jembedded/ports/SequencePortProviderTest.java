package io.josslab.redis.jembedded.ports;

import io.josslab.redis.jembedded.builder.PortProvider;
import org.junit.jupiter.api.Test;

import static io.josslab.redis.jembedded.builder.PortProvider.newSequencePortProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
