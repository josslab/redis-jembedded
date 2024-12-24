package io.josslab.redis.jembedded.ports;

import io.josslab.redis.jembedded.builder.PortProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.josslab.redis.jembedded.builder.PortProvider.newEphemeralPortProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EphemeralPortProviderTest {

  @Test
  void nextShouldGiveNextFreeEphemeralPort() {
    final int portCount = 20;
    final PortProvider provider = newEphemeralPortProvider();

    final List<Integer> ports = new ArrayList<>();
    for (int i = 0; i < portCount; i++) {
      ports.add(provider.get());
    }

    assertEquals(portCount, ports.size());
  }

}
