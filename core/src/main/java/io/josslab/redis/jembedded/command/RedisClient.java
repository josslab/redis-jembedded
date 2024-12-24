package io.josslab.redis.jembedded.command;

import io.josslab.redis.jembedded.error.RedisCommandException;
import io.josslab.redis.jembedded.fi.IOSupplier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;

import static io.josslab.redis.jembedded.utils.ExceptionHandler.supply;

/**
 * A lightweight implementation of the Redis server <a href="https://redis.io/topics/protocol">protocol</a>
 * <p>
 * Effectively a complete RedisClient client implementation.
 */
public class RedisClient implements AutoCloseable {

  /**
   *
   */
  private Socket socket;

  /**
   * Used for writing the data to the server.
   */
  private RespEncoder writer;

  /**
   * Used for reading responses from the server.
   */
  private RespDecoder reader;

  /**
   * Construct the connection with the specified Socket as the server connection.
   *
   * @param host host to connect
   * @param port port to connect
   */
  public RedisClient(String host, int port) {
    this(() -> new Socket(host, port));
  }

  public RedisClient(Proxy proxy) {
    this(() -> new Socket(proxy));
  }

  /**
   * Construct the connection with the socket factory
   *
   * @param socketFactory socket factory
   */
  public RedisClient(IOSupplier<Socket> socketFactory) {
    this.socket = supply(socketFactory);
    this.reader = new RespDecoder(new BufferedInputStream(
      supply(() -> socket.getInputStream())
    ));
    this.writer = new RespEncoder(new BufferedOutputStream(
      supply(() -> socket.getOutputStream())
    ));
  }

  /**
   * Execute a Redis command and return its result.
   *
   * @param args Command and arguments to pass into redis.
   * @param <T>  The expected result type
   * @return Result of redis.
   * @throws RedisCommandException All protocol and io errors are IO exceptions.
   */
  public <T> T call(Object... args) throws RedisCommandException {
    try {
      writer.write(Arrays.asList(args));
      writer.flush();
      return read();
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause instanceof RespDecoder.ServerError && isMoveMessage(cause.getMessage())) {
        close();
        moveConnect(cause.getMessage());
        return call(args);
      } else if (e instanceof RedisCommandException) {
        throw (RedisCommandException) e;
      }
      throw new RedisCommandException(e);
    }
  }

  private boolean isMoveMessage(String msg) {
    return msg.startsWith("MOVED");
  }

  private void moveConnect(String msg) {
    String address = msg.split(" ")[2];
    try {
      URI uri = new URI("redis://" + address);
      InetAddress inetAddress = InetAddress.getByName(uri.getHost());
      this.socket = new Socket(inetAddress, uri.getPort());
      this.reader = new RespDecoder(new BufferedInputStream(socket.getInputStream()));
      this.writer = new RespEncoder(new BufferedOutputStream(socket.getOutputStream()));
    } catch (Exception e) {
      throw new RedisCommandException(e);
    }
  }

  /**
   * Does a blocking read to wait for redis to send data.
   *
   * @param <T> The expected result type.
   * @return Result of redis
   * @throws RedisCommandException Propagated
   */
  @SuppressWarnings({"unchecked"})
  public <T> T read() {
    try {
      return (T) reader.parse();
    } catch (Exception e) {
      throw new RedisCommandException(e);
    }
  }

  @Override
  public void close() {
    try {
      socket.close();
    } catch (Exception e) {
      throw new RedisCommandException(e);
    }
  }

}
