package io.josslab.redis.jembedded.command;

import java.util.List;

public class RedisCommand implements AutoCloseable {

  public static final String CLUSTER = "CLUSTER";

  private final RedisClient client;

  public RedisCommand(String host, int port) {
    this(new RedisClient(host, port));
  }

  public RedisCommand(RedisClient client) {
    this.client = client;
  }

  @Override
  public void close() {
    client.close();
  }

  public void clusterMeet(String host, int port) {
    client.call(CLUSTER, "MEET", host, String.valueOf(port));
  }

  public String clusterMyId() {
    return client.call(RedisCommand.CLUSTER, "MYID");
  }

  public void clusterAddSlots(int start, int end) {
    int dif = end - start + 1;
    Object[] args = new Object[dif + 2];
    args[0] = CLUSTER;
    args[1] = "ADDSLOTS";
    for (int i = start; i <= end; ++i) {
      args[i % dif + 2] = i;
    }
    client.call(args);
  }

  public String clusterNodes() {
    return client.call(RedisCommand.CLUSTER, "NODES");
  }

  public String clusterInfo() {
    return client.call(RedisCommand.CLUSTER, "INFO");
  }

  public void clusterReplicate(String nodeId) {
    client.call(RedisCommand.CLUSTER, "REPLICATE", nodeId);
  }

  public Object get(String key) {
    return client.call("GET", key);
  }

  public List<Object> mget(String... keys) {
    return client.call("MGET", keys);
  }

  public Object set(String key, String value) {
    return client.call("SET", key, value);
  }
}
