package io.josslab.redis.jembedded.services;

public class UnknownOSName extends RuntimeException {

  public UnknownOSName(final String name) {
    super("Unrecognized OS: " + name);
  }

}
