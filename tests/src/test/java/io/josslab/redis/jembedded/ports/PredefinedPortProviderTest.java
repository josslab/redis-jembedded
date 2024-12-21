package io.josslab.redis.jembedded.ports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.josslab.redis.jembedded.core.PortProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.josslab.redis.jembedded.core.PortProvider.newPredefinedPortProvider;

class PredefinedPortProviderTest {

  @Test
  void nextShouldGiveNextPortFromAssignedList() {
    final Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    final PortProvider provider = newPredefinedPortProvider(ports);

    final List<Integer> returnedPorts = new ArrayList<>();
    for (int i = 0; i < ports.size(); i++) {
      returnedPorts.add(provider.get());
    }

    assertEquals(ports, returnedPorts);
  }

  @Test
  void nextShouldThrowExceptionWhenRunOutsOfPorts() {
    final Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    final PortProvider provider = newPredefinedPortProvider(ports);

    for (int i = 0; i < ports.size(); i++) {
      provider.get();
    }

    Assertions.assertThrows(IllegalArgumentException.class, provider::get);
  }

}
