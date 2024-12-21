package io.josslab.redis.jembedded;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.josslab.redis.jembedded.command.RedisClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static io.josslab.redis.jembedded.RedisServer.newRedisServer;

class RedisServerClusterTest {

  private RedisServer redisServer1;
  private RedisServer redisServer2;

  @BeforeEach
  void setUp() throws IOException {
    redisServer1 = newRedisServer().port(6300).build();
    redisServer2 = newRedisServer().port(6301)
      .slaveOf("localhost", 6300)
      .build();

    redisServer1.start();
    redisServer2.start();
  }

  @Test
  void testSimpleOperationsAfterRun() {
    try (RedisClient command = new RedisClient("localhost", 6300)) {
      command.call("SET", "abc", "1");

      assertEquals("1", command.call("GET", "abc"));
      assertNull(command.call("GET", "def"));
    }
  }


  @AfterEach
  void tearDown() throws IOException {
    redisServer1.stop();
    redisServer2.stop();
  }

}
