package io.josslab.redis.jembedded.error;

public class RedisClusterSetupException extends RuntimeException {

  public RedisClusterSetupException(final String message) {
    super(message);
  }

  public RedisClusterSetupException(Throwable cause) {
    super(cause);
  }

  public RedisClusterSetupException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
