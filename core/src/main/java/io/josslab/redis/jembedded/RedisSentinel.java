package io.josslab.redis.jembedded;

import io.josslab.redis.jembedded.builder.RedisSentinelBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class RedisSentinel extends RedisInstance {

  public RedisSentinel(final String bindAddress, final int port, final List<String> args, final boolean forceStop) {
    super(bindAddress, port, args, SENTINEL_READY_PATTERN, forceStop, null, null);
  }

  public RedisSentinel(final String bindAddress, final int port, final List<String> args, final boolean forceStop,
                       final Consumer<String> soutListener, final Consumer<String> serrListener) {
    super(bindAddress, port, args, SENTINEL_READY_PATTERN, forceStop, soutListener, serrListener);
  }

  public static RedisSentinelBuilder newRedisSentinel() {
    return new RedisSentinelBuilder();
  }

}
