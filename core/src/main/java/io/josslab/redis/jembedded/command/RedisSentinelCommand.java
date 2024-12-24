package io.josslab.redis.jembedded.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.josslab.redis.jembedded.utils.Objects.requireNonNullElse;
import static java.util.Collections.emptyList;

/**
 * Implementation of redis sentinel command
 * <a href="https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/#sentinel-commands">sentinel command</a>
 */
public class RedisSentinelCommand implements AutoCloseable {

  public static final String SENTINEL = "SENTINEL";

  private final RedisClient client;

  public RedisSentinelCommand(String host, int port) {
    this(new RedisClient(host, port));
  }

  public RedisSentinelCommand(RedisClient client) {
    this.client = client;
  }

  @Override
  public void close() {
    client.close();
  }

  public String myId() {
    return client.call(SENTINEL, "MYID");
  }

  public List<Map<String, String>> masters() {
    return requireNonNullElse(client.call(SENTINEL, "MASTERS"), emptyList())
      .stream()
      .map(BuilderFactory.STRING_MAP::build)
      .collect(Collectors.toList());
  }

  public Map<String, String> master(String master) {
    List<Object> reply = client.call(SENTINEL, "MASTER", master);
    return BuilderFactory.STRING_MAP.build(reply);
  }

  public List<Map<String, String>> sentinels() {
    return requireNonNullElse(client.call(SENTINEL, "SENTINELS"), emptyList())
      .stream()
      .map(BuilderFactory.STRING_MAP::build)
      .collect(Collectors.toList());
  }

  public List<String> getMasterAddrByName(String master) {
    return client.call(SENTINEL, "GET-MASTER-ADDR-BY-NAME", master);
  }
}
