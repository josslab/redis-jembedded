package io.josslab.redis.jembedded;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.josslab.redis.jembedded.command.RedisClient;
import io.josslab.redis.jembedded.command.RedisCommand;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.josslab.redis.jembedded.RedisShardedCluster.newRedisCluster;

class RedisShardedClusterTest {
  private RedisShardedCluster cluster;

  @BeforeEach
  public void setUp() throws IOException {
    cluster = newRedisCluster()
      .shard("master1", 1)
      .shard("master2", 1)
      .shard("master3", 1)
      .build();
    cluster.start();
  }

  @Test
  void testSimpleOperationsAfterClusterStart() {
    try (final RedisClient command = new RedisClient("127.0.0.1", cluster.getPort())) {
      command.call("SET", "some_key", "some_value");
      assertEquals("some_value", command.call("GET", "some_key"));
    }
  }

  @Test
  void testSimpleOperationsAfterClusterWithEphemeralPortsStart() throws IOException {
    cluster.stop();
    cluster = newRedisCluster().ephemeral()
      .shard("master1", 1)
      .shard("master2", 1)
      .shard("master3", 1)
      .build();
    cluster.start();
    try (RedisCommand command = new RedisCommand("127.0.0.1", cluster.getPort())) {
      command.set("some_key", "some_value");
      assertEquals("some_value", command.get("some_key"));
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    cluster.stop();
  }

}
