package io.josslab.redis.jembedded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import io.josslab.redis.jembedded.core.RedisSentinelBuilder;
import io.josslab.redis.jembedded.utils.NetUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static io.josslab.redis.jembedded.RedisCluster.newRedisCluster;
import static io.josslab.redis.jembedded.RedisSentinel.newRedisSentinel;
import static io.josslab.redis.jembedded.utils.Collections.newHashSet;

class RedisClusterTest {
  private final RedisSentinelBuilder sentinelBuilder = newRedisSentinel();
  private String bindAddress;

  private Redis sentinel1;
  private Redis sentinel2;
  private Redis master1;
  private Redis master2;

  private RedisCluster instance;

  @BeforeEach
  void setUp() throws UnknownHostException {
    sentinel1 = mock(Redis.class);
    sentinel2 = mock(Redis.class);
    master1 = mock(Redis.class);
    master2 = mock(Redis.class);

    if (bindAddress == null) {
      bindAddress = Inet4Address.getLocalHost().getHostAddress();
      sentinelBuilder.bind(bindAddress);
    }
  }

  @Test
  void stopShouldStopEntireCluster() throws IOException {
    final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
    final List<Redis> servers = Arrays.asList(master1, master2);
    instance = new RedisCluster(sentinels, servers);

    instance.stop();

    for (final Redis s : sentinels) {
      verify(s).stop();
    }
    for (final Redis s : servers) {
      verify(s).stop();
    }
  }

  @Test
  void startShouldStartEntireCluster() throws IOException {
    final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
    final List<Redis> servers = Arrays.asList(master1, master2);
    instance = new RedisCluster(sentinels, servers);

    instance.start();

    for (final Redis s : sentinels) {
      verify(s).start();
    }
    for (final Redis s : servers) {
      verify(s).start();
    }
  }

  @Test
  void isActiveShouldCheckEntireClusterIfAllActive() {
    given(sentinel1.isActive()).willReturn(true);
    given(sentinel2.isActive()).willReturn(true);
    given(master1.isActive()).willReturn(true);
    given(master2.isActive()).willReturn(true);
    final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
    final List<Redis> servers = Arrays.asList(master1, master2);
    instance = new RedisCluster(sentinels, servers);

    instance.isActive();

    for (final Redis s : sentinels) {
      verify(s).isActive();
    }
    for (final Redis s : servers) {
      verify(s).isActive();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithSingleMasterNoSlavesCluster() throws IOException {
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelCount(1)
      .replicationGroup("ourmaster", 0)
      .build();
    cluster.start();

    try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", newHashSet(bindAddress + ":26379"))) {
      testPool(pool);
    } finally {
      cluster.stop();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithSingleMasterAndOneSlave() throws IOException {
    int sentinelStartingPort = NetUtils.allocatePort(0);
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelCount(1)
      .sentinelStartingPort(sentinelStartingPort)
      .replicationGroup("ourmaster", 1)
      .build();
    cluster.start();

    try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", newHashSet(bindAddress + ":" + sentinelStartingPort))) {
      testPool(pool);
    } finally {
      cluster.stop();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithSingleMasterMultipleSlaves() throws IOException {
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelCount(1)
      .replicationGroup("ourmaster", 2)
      .build();
    cluster.start();

    try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", newHashSet(bindAddress + ":26379"))) {
      testPool(pool);
    } finally {
      cluster.stop();
    }
  }


  @Test
  void testSimpleOperationsAfterRunWithTwoSentinelsSingleMasterMultipleSlaves() throws IOException {
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelCount(2)
      .replicationGroup("ourmaster", 2)
      .build();
    cluster.start();

    try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", newHashSet(bindAddress + ":26379", bindAddress + ":26380"))) {
      testPool(pool);
    } finally {
      cluster.stop();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithTwoPredefinedSentinelsSingleMasterMultipleSlaves() throws IOException {
    final List<Integer> sentinelPorts = Arrays.asList(26381, 26382);
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelPorts(sentinelPorts)
      .replicationGroup("ourmaster", 2)
      .build();
    cluster.start();
    final Set<String> sentinelHosts = cluster.sentinelPorts()
      .stream()
      .map(port -> bindAddress + ":" + port)
      .collect(Collectors.toSet());

    try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", sentinelHosts)) {
      testPool(pool);
    } finally {
      cluster.stop();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterCluster() throws IOException {
    final String master1 = "master1";
    final String master2 = "master2";
    final String master3 = "master3";
    final RedisCluster cluster = newRedisCluster()
      .withSentinelBuilder(sentinelBuilder)
      .sentinelCount(3)
      .quorumSize(2)
      .replicationGroup(master1, 1)
      .replicationGroup(master2, 1)
      .replicationGroup(master3, 1)
      .build();
    cluster.start();

    try (
      JedisSentinelPool pool1 = new JedisSentinelPool(master1, newHashSet(bindAddress + ":26379", bindAddress + ":26380", bindAddress + ":26381"));
      JedisSentinelPool pool2 = new JedisSentinelPool(master2, newHashSet(bindAddress + ":26379", bindAddress + ":26380", bindAddress + ":26381"));
      JedisSentinelPool pool3 = new JedisSentinelPool(master3, newHashSet(bindAddress + ":26379", bindAddress + ":26380", bindAddress + ":26381"))
    ) {
      testPool(pool1);
      testPool(pool2);
      testPool(pool3);
    } finally {
      cluster.stop();
    }
  }

  @Test
  void testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterEphemeralCluster() throws IOException {
    final String master1 = "master1";
    final String master2 = "master2";
    final String master3 = "master3";
    final RedisCluster cluster = newRedisCluster().withSentinelBuilder(sentinelBuilder)
      .ephemeral().sentinelCount(3).quorumSize(2)
      .replicationGroup(master1, 1)
      .replicationGroup(master2, 1)
      .replicationGroup(master3, 1)
      .build();
    cluster.start();
    final Set<String> sentinelHosts = cluster.sentinelPorts()
      .stream()
      .map(port -> bindAddress + ":" + port)
      .collect(Collectors.toSet());

    try (
      JedisSentinelPool pool1 = new JedisSentinelPool(master1, sentinelHosts);
      JedisSentinelPool pool2 = new JedisSentinelPool(master2, sentinelHosts);
      JedisSentinelPool pool3 = new JedisSentinelPool(master3, sentinelHosts)
    ) {
      testPool(pool1);
      testPool(pool2);
      testPool(pool3);
    } finally {
      cluster.stop();
    }
  }

  private void testPool(final JedisSentinelPool pool) {
    try (Jedis jedis = pool.getResource()) {
      jedis.mset("abc", "1", "def", "2");

      assertEquals("1", jedis.mget("abc").get(0));
      assertEquals("2", jedis.mget("def").get(0));
      assertNull(jedis.mget("xyz").get(0));
    }
  }

}
