package io.josslab.redis.jembedded.command;

public final class Mark {

  public static final char CR = '\r';
  public static final char LF = '\n';
  public static final char STRING = '+';
  public static final char ERROR = '-';
  public static final char INTEGER = ':';
  public static final char BULK_STRING = '$';
  public static final char ARRAY = '*';
  public static final char NULL = '_';
  public static final char BOOLEAN = '#';
  public static final char DOUBLE = ',';
  public static final char BIG_INTEGER = '(';
  public static final char BULK_ERROR = '!';
  public static final char VERBATIM_STRING = '=';
  public static final char MAP = '%';
  public static final char SET = '~';
  public static final char PUSH = '>';
  public static final byte[] CRLF = new byte[]{'\r', '\n'};
  public static final char BOOLEAN_TRUE = 't';
  public static final char BOOLEAN_FALSE = 'f';

  private Mark() {
    // ignored
  }

  public static char getBoolean(boolean b) {
    return b ? BOOLEAN_TRUE : BOOLEAN_FALSE;
  }
}
