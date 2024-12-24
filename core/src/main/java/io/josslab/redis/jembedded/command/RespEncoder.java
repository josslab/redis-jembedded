package io.josslab.redis.jembedded.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;

import static io.josslab.redis.jembedded.command.Mark.*;
import static java.util.function.Function.identity;

/**
 * Implements the encoding (writing) side.
 */
public class RespEncoder {

  /**
   * This stream we will write to.
   */
  private final OutputStream out;

  /**
   * Construct the encoder with the passed output stream the encoder will write to.
   *
   * @param out Will be used to write all encoded data to.
   */
  public RespEncoder(OutputStream out) {
    this.out = out;
  }

  void write(byte[] val) throws IOException {
    write(val, identity());
  }

  void write(String val) throws IOException {
    write(val, String::getBytes);
  }

  void write(Integer val) throws IOException {
    write(val, i -> String.valueOf(i).getBytes());
  }

  void write(Long val) throws IOException {
    write(val, i -> String.valueOf(i).getBytes());
  }

  void write(Boolean val) throws IOException {
    int boolAsInt = Boolean.TRUE.equals(val) ? 1 : 0;
    write(val, i -> String.valueOf(boolAsInt).getBytes());
  }

  <T> void write(T value, Function<T, byte[]> byteArrayFactory) throws IOException {
    if (value == null) {
      writeNull();
      return;
    }
    byte[] byteArray = byteArrayFactory.apply(value);
    out.write(BULK_STRING);
    out.write(Integer.toString(byteArray.length).getBytes());
    writeCLRF();
    out.write(byteArray);
    writeCLRF();
  }

  void writeNull() throws IOException {
    out.write(BULK_STRING);
    out.write('0');
    writeCLRF();
    writeCLRF();
  }

  void writeCLRF() throws IOException {
    out.write(CRLF);
  }

  /**
   * Write a list of objects in the "RESP Arrays" format.
   *
   * @param list A list of objects that contains Strings, Longs, Integers and (recursively) Lists.
   * @throws IOException              Propagated from the output stream.
   * @throws IllegalArgumentException If the list contains unencodable objects.
   * @link <a href="https://redis.io/topics/protocol#resp-arrays">RESP Arrays</a>
   */
  public void write(List<?> list) throws IOException, IllegalArgumentException {
    out.write(ARRAY);
    out.write(Long.toString(list.size()).getBytes());
    out.write(CRLF);

    for (Object o : list) {
      if (o instanceof byte[]) {
        write((byte[]) o);
      } else if (o instanceof String) {
        write((String) o);
      } else if (o instanceof Long) {
        write((Long) o);
      } else if (o instanceof Integer) {
        write((Integer) o);
      } else if (o instanceof Boolean) {
        write((Boolean) o);
      } else if (o instanceof List) {
        write((List<?>) o);
      } else {
        throw new IllegalArgumentException("Unexpected type " + o.getClass().getCanonicalName());
      }
    }
  }

  public void flush() throws IOException {
    out.flush();
  }
}
