package io.josslab.redis.jembedded;

import io.josslab.redis.jembedded.command.RedisClient;
import io.josslab.redis.jembedded.services.ExecutableProperty;
import io.josslab.redis.jembedded.services.ExecutableProvider;
import io.josslab.redis.jembedded.testkit.assertion.AssertConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static io.josslab.redis.jembedded.RedisServer.newRedisServer;
import static org.junit.jupiter.api.Assertions.*;

class RedisServerTest {

  private RedisServer redisServer;

  @BeforeEach
  void setUp() throws Exception {
    redisServer = RedisServer.newRedisServer().build();
  }

  @Test
  @Timeout(value = 1500L, unit = TimeUnit.MILLISECONDS)
  void testSimpleRun() throws Exception {
    redisServer.start();
    try (final RedisClient client = new RedisClient("127.0.0.1", redisServer.port())) {
      Assertions.assertNull(client.call("GET", "some_key"));
    }
    redisServer.stop();
  }

  @Test
  void shouldAllowMultipleRunsWithoutStop() throws IOException {
    try {
      redisServer.start();
      redisServer.start();
      AssertConnection.assertSocketAvailable(redisServer.bindAddress(), redisServer.port());
    } finally {
      redisServer.stop();
    }
  }

  @Test
  void shouldAllowSubsequentRuns() throws IOException {
    redisServer.start();
    AssertConnection.assertSocketAvailable(redisServer.bindAddress(), redisServer.port());
    redisServer.stop();

    redisServer.start();
    AssertConnection.assertSocketAvailable(redisServer.bindAddress(), redisServer.port());
    redisServer.stop();

    redisServer.start();
    AssertConnection.assertSocketAvailable(redisServer.bindAddress(), redisServer.port());
    redisServer.stop();
  }

  @Test
  void testSimpleOperationsAfterRun() throws IOException {
    redisServer.start();

    try (RedisClient command = new RedisClient("localhost", redisServer.port())) {
      command.call("HSET", "simple_map", "key1", "value1");
      assertInstanceOf(LinkedList.class, command.call("HGETALL", "simple_map"));
      assertEquals("value1", command.call("HGET", "simple_map", "key1"));
    } finally {
      redisServer.stop();
    }
  }

  @Test
  void shouldIndicateInactiveBeforeStart() {
    assertFalse(redisServer.isActive());
  }

  @Test
  void shouldIndicateActiveAfterStart() throws IOException {
    redisServer.start();
    assertTrue(redisServer.isActive());
    redisServer.stop();
  }

  @Test
  void shouldIndicateInactiveAfterStop() throws IOException {
    redisServer.start();
    redisServer.stop();
    assertFalse(redisServer.isActive());
  }

  @Test
  void shouldOverrideDefaultExecutable() throws IOException {
    String tempDir = System.getProperty("java.io.tmpdir");
    File executable = new File(tempDir + "/redis-server-test");
    ExecutableProvider provider = new ExecutableProvider() {
      @Override
      public File getExecutable() {
        return executable;
      }

      @Override
      public ExecutableProperty getProperty() {
        return null;
      }
    };
    redisServer = newRedisServer()
      .executableProvider(provider)
      .build();
    try {
      assertNotNull(redisServer);
    } finally {
      executable.delete();
    }
  }

  @Test
  void testAwaitRedisServerReady() throws IOException {
    testReadyPattern("/redis-2.x-standalone-startup-output.txt");
    testReadyPattern("/redis-3.x-standalone-startup-output.txt");
    testReadyPattern("/redis-4.x-standalone-startup-output.txt");
  }

  private static void testReadyPattern(final String resourcePath) throws IOException {
    final InputStream in = RedisServerTest.class.getResourceAsStream(resourcePath);
    assertNotNull(in);
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      do {
        line = reader.readLine();
        assertNotNull(line);
      } while (!Redis.SERVER_READY_PATTERN.matcher(line).matches());
    }
  }

}
