package io.josslab.redis.jembedded.error;

public class RedisCommandException extends RuntimeException {

  public RedisCommandException() {
    super();
  }

  public RedisCommandException(String message) {
    super(message);
  }

  public RedisCommandException(Throwable cause) {
    super(cause);
  }

  public RedisCommandException(String message, Throwable cause) {
    super(message, cause);
  }
}
