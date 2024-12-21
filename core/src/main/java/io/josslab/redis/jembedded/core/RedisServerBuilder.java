package io.josslab.redis.jembedded.core;

import io.josslab.redis.jembedded.RedisServer;
import io.josslab.redis.jembedded.services.ExecutableLoader;
import io.josslab.redis.jembedded.services.ExecutableProvider;
import io.josslab.redis.jembedded.utils.NetUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.josslab.redis.jembedded.Redis.DEFAULT_REDIS_PORT;

public final class RedisServerBuilder {

  private static final String LINE_SEPARATOR = System.lineSeparator();

  private ExecutableProvider provider = ExecutableLoader.loadExecutableProvider();
  private String bindAddress = "127.0.0.1";
  private int bindPort = DEFAULT_REDIS_PORT;
  private InetSocketAddress slaveOf;
  private boolean forceStop = false;
  private Consumer<String> soutListener;
  private Consumer<String> serrListener;

  private final StringBuilder redisConfigBuilder = new StringBuilder();

  public RedisServerBuilder executableProvider(final ExecutableProvider provider) {
    this.provider = provider;
    return this;
  }

  public RedisServerBuilder bind(final String bind) {
    this.bindAddress = bind;
    return this;
  }

  public RedisServerBuilder port(final int port) {
    this.bindPort = port;
    return this;
  }

  public RedisServerBuilder slaveOf(final String hostname, final int port) {
    this.slaveOf = new InetSocketAddress(hostname, port);
    return this;
  }

  public RedisServerBuilder slaveOf(final InetSocketAddress slaveOf) {
    this.slaveOf = slaveOf;
    return this;
  }

  public RedisServerBuilder configFile(final String redisConf) throws IOException {
    return configFile(Paths.get(redisConf));
  }

  public RedisServerBuilder configFile(final Path redisConf) throws IOException {
    try (Stream<String> streamLines = Files.lines(redisConf)) {
      streamLines.forEach(line -> redisConfigBuilder.append(line).append(LINE_SEPARATOR));
    }
    return this;
  }

  public RedisServerBuilder settingIf(final boolean shouldSet, final String configLine) {
    if (shouldSet) setting(configLine);
    return this;
  }

  public RedisServerBuilder setting(final String configLine) {
    redisConfigBuilder.append(configLine).append(LINE_SEPARATOR);
    return this;
  }

  public RedisServerBuilder onShutdownForceStop(final boolean forceStop) {
    this.forceStop = forceStop;
    return this;
  }

  public RedisServerBuilder soutListener(final Consumer<String> soutListener) {
    this.soutListener = soutListener;
    return this;
  }

  public RedisServerBuilder serrListener(final Consumer<String> serrListener) {
    this.serrListener = serrListener;
    return this;
  }

  public RedisServer build() throws IOException {
    bindPort = NetUtils.allocatePort(bindPort);
    return new RedisServer(bindAddress, bindPort, buildCommandArgs(), forceStop, soutListener, serrListener);
  }

  public void reset() {
    this.slaveOf = null;
  }

  public List<String> buildCommandArgs() throws IOException {
    setting("bind " + bindAddress);
    File executable = provider.getExecutable();

    final Path redisConfigFile =
      writeNewRedisConfigFile(executable, "redis-jembedded-server_" + bindPort, redisConfigBuilder.toString());

    final List<String> args = new ArrayList<>();
    args.add(executable.getAbsolutePath());
    args.add(redisConfigFile.toAbsolutePath().toString());
    args.add("--port");
    args.add(Integer.toString(bindPort));

    if (slaveOf != null) {
      args.add("--slaveof");
      args.add(slaveOf.getHostName());
      args.add(Integer.toString(slaveOf.getPort()));
    }

    return args;
  }

  private static Path writeNewRedisConfigFile(final File executableDir, final String prefix, final String contents) throws IOException {
    Path executablePath = executableDir.toPath().getParent();
    final Path redisConfigFile = Files.createTempFile(executablePath, prefix, ".conf");
    redisConfigFile.toFile().deleteOnExit();
    Files.write(redisConfigFile, contents.getBytes());
    return redisConfigFile;
  }

}
