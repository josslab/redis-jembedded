package io.josslab.redis.jembedded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import io.josslab.redis.jembedded.command.RedisCommand;
import io.josslab.redis.jembedded.command.RedisSentinelCommand;
import io.josslab.redis.jembedded.testkit.assertion.AssertConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RedisSentinelTest {

  private String bindAddress;
  private RedisSentinel sentinel;
  private RedisServer server;

  @BeforeEach
  void setup() throws Exception {
    if (bindAddress == null) {
      bindAddress = Inet4Address.getLocalHost().getHostAddress();
    }
  }

  @Test
  @Timeout(value = 3000L, unit = TimeUnit.MILLISECONDS)
  void testSimpleRun() throws IOException {
    server = RedisServer.newRedisServer().build();
    sentinel = RedisSentinel.newRedisSentinel().bind(bindAddress).build();
    sentinel.start();
    server.start();
    AssertConnection.assertSocketAvailable(server.bindAddress(), server.port());
    AssertConnection.assertSocketAvailable(sentinel.bindAddress(), sentinel.port());

    server.stop();
    sentinel.stop();
  }

  @Test
  void shouldAllowSubsequentRuns() throws IOException {
    sentinel = RedisSentinel.newRedisSentinel().bind(bindAddress).build();
    sentinel.start();
    AssertConnection.assertSocketAvailable(sentinel.bindAddress(), sentinel.port());
    sentinel.stop();

    sentinel.start();
    AssertConnection.assertSocketAvailable(sentinel.bindAddress(), sentinel.port());
    sentinel.stop();

    sentinel.start();
    AssertConnection.assertSocketAvailable(sentinel.bindAddress(), sentinel.port());
    sentinel.stop();
  }

  @Test
  void testSimpleOperationsAfterRun() throws IOException {
    server = RedisServer.newRedisServer()
      .build();
    sentinel = RedisSentinel.newRedisSentinel()
      .bind(bindAddress)
      .masterPort(server.port())
      .build();
    server.start();
    sentinel.start();

    String masterAddr;
    int masterPort;
    try (RedisSentinelCommand sentinelCommand = new RedisSentinelCommand(bindAddress, 26379)) {
      List<String> reply = sentinelCommand.getMasterAddrByName("mymaster");
      masterAddr = reply.get(0);
      masterPort = Integer.parseInt(reply.get(1));
    }

    try (RedisCommand command = new RedisCommand(masterAddr, masterPort)) {
      command.set("abc", "1");
      command.set("def", "2");

      assertEquals("1", command.get("abc"));
      assertEquals("2", command.get("def"));
      assertNull(command.get("xyz"));
    } finally {
      sentinel.stop();
      server.stop();
    }
  }

  @Test
  void testAwaitRedisSentinelReady() throws Exception {
    assertReadyPattern("/redis-2.x-sentinel-startup-output.txt");
    assertReadyPattern("/redis-3.x-sentinel-startup-output.txt");
    assertReadyPattern("/redis-4.x-sentinel-startup-output.txt");
  }

  private static void assertReadyPattern(final String resourcePath) throws IOException {
    final InputStream in = RedisServerTest.class.getResourceAsStream(resourcePath);
    assertNotNull(in);
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      do {
        line = reader.readLine();
        assertNotNull(line);
      } while (!Redis.SENTINEL_READY_PATTERN.matcher(line).matches());
    }
  }

}
