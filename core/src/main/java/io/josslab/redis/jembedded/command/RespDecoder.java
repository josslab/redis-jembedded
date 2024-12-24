package io.josslab.redis.jembedded.command;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

import static io.josslab.redis.jembedded.command.Mark.*;

/**
 * Implements the decoder (reader) side of protocol.
 */
public class RespDecoder {

  /**
   * Thrown whenever data could not be parsed.
   */
  static class ProtocolException extends IOException {
    ProtocolException(String msg) {
      super(msg);
    }
  }

  /**
   * Thrown whenever an error string is decoded.
   */
  static class ServerError extends IOException {
    ServerError(String msg) {
      super(msg);
    }
  }

  /**
   * The input stream used to read the data from.
   */
  private final InputStream input;

  /**
   * Constructor.
   *
   * @param input The stream to read the data from.
   */
  public RespDecoder(InputStream input) {
    this.input = input;
  }

  /**
   * Parse incoming data from the stream.
   * <p>
   * Based on each of the markers which will identify the type of data being sent, the parsing
   * is delegated to the type-specific methods.
   *
   * @return The parsed object
   * @throws IOException       Propagated from the stream
   * @throws ProtocolException In case unexpected bytes are encountered.
   */
  public Object parse() throws IOException {
    Object ret;
    int read = this.input.read();
    switch (read) {
      case STRING:
        ret = this.parseSimpleString();
        break;
      case ERROR:
        throw new ServerError(this.parseSimpleString());
      case INTEGER:
        ret = this.parseNumber();
        break;
      case BULK_STRING:
        ret = this.parseBulkString();
        break;
      case ARRAY:
        long len = this.parseNumber();
        if (len == -1) {
          ret = null;
        } else {
          List<Object> arr = new LinkedList<>();
          for (long i = 0; i < len; i++) {
            arr.add(this.parse());
          }
          ret = arr;
        }
        break;
      case BOOLEAN:
        ret = this.parseBoolean();
        break;
      case -1:
      case NULL:
        return null;
      case DOUBLE:
        ret = this.parseDouble();
        break;
      case BIG_INTEGER:
        ret = this.parseBigInteger();
        break;
      case MAP:
        ret = this.parseMap();
        break;
      case SET:
        ret = this.parseSet();
        break;
      default:
        throw new ProtocolException("Unexpected input: " + (byte) read);
    }

    return ret;
  }

  /**
   * Parse "RESP Bulk string" as a String object.
   *
   * @return The parsed response
   * @throws IOException Propagated from underlying stream.
   */
  private String parseBulkString() throws IOException {
    final long expectedLength = parseNumber();
    if (expectedLength == -1) {
      return null;
    }
    if (expectedLength > Integer.MAX_VALUE) {
      throw new ProtocolException("Unsupported value length for bulk string");
    }
    final int numBytes = (int) expectedLength;
    final byte[] buffer = new byte[numBytes];
    int read = 0;
    while (read < expectedLength) {
      read += input.read(buffer, read, numBytes - read);
    }
    expectCRLF(input);
    return new String(buffer);
  }

  /**
   * Parse "RESP Simple String"
   *
   * @return Resultant string
   * @throws IOException Propagated from underlying stream.
   */
  private String parseSimpleString() throws IOException {
    return new String(scanCr());
  }

  /**
   * Parse a number (as long)
   *
   * @return The number
   * @throws IOException Propagated from underlying stream
   */
  private long parseNumber() throws IOException {
    return Long.parseLong(new String(scanCr()));
  }

  /**
   * Parse a boolean
   *
   * @return The boolean
   * @throws IOException Propagated from underlying stream.
   */
  private boolean parseBoolean() throws IOException {
    int boolChar = input.read();
    expectCRLF(input);
    return boolChar == BOOLEAN_TRUE;
  }

  /**
   * Parse a double
   *
   * @return The boolean
   * @throws IOException Propagated from underlying stream.
   */
  private double parseDouble() throws IOException {
    return Double.parseDouble(new String(scanCr()));
  }

  private BigInteger parseBigInteger() throws IOException {
    return new BigInteger(new String(scanCr()));
  }

  /**
   * Parse a "RESP3 Map"
   *
   * @return The map
   * @throws IOException Propagated from underlying stream.
   */
  private Map<Object, Object> parseMap() throws IOException {
    final long length = parseNumber();
    Map<Object, Object> map = new LinkedHashMap<>();
    if (length == -1) {
      return map;
    }
    for (int i = 0; i < length; i++) {
      Object key = parse();
      Object value = parse();
      map.put(key, value);
    }
    return map;
  }

  /**
   * Parse a "RESP3 Set"
   *
   * @return The set
   * @throws IOException Propagated from underlying stream.
   */
  private Set<Object> parseSet() throws IOException {
    final long length = parseNumber();
    Set<Object> set = new LinkedHashSet<>();
    if (length == -1) {
      return set;
    }
    for (int i = 0; i < length; i++) {
      set.add(parse());
    }
    return set;
  }

  /**
   * Scan the input stream for the next CR character
   *
   * @return Byte array.
   * @throws IOException Propagated from underlying stream
   */
  private byte[] scanCr() throws IOException {
    int ch;
    List<Byte> byteList = new LinkedList<Byte>();
    while ((ch = input.read()) != CR) {
      byteList.add((byte) ch);
    }
    if (input.read() != LF) {
      throw new ProtocolException("Expected LF");
    }
    int size = byteList.size();
    byte[] result = new byte[size];
    for (int i = 0; i < size; ++i) {
      result[i] = byteList.get(i);
    }
    return result;
  }

  private static void expectCRLF(InputStream input) throws IOException {
    if (input.read() != CR) {
      throw new ProtocolException("Expected CR");
    }
    if (input.read() != LF) {
      throw new ProtocolException("Expected LF");
    }
  }
}
