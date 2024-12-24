package io.josslab.redis.jembedded;

import io.josslab.redis.jembedded.builder.RedisServerBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class RedisServer extends RedisInstance {

  public RedisServer(final String bindAddress, final int port, final List<String> args, final boolean forceStop) {
    super(bindAddress, port, args, SERVER_READY_PATTERN, forceStop, null, null);
  }

  public RedisServer(final String bindAddress, final int port, final List<String> args, final boolean forceStop,
                     final Consumer<String> soutListener, final Consumer<String> serrListener) {
    super(bindAddress, port, args, SERVER_READY_PATTERN, forceStop, soutListener, serrListener);
  }

  public static RedisServerBuilder newRedisServer() {
    return new RedisServerBuilder();
  }

}
