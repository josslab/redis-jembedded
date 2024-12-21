package io.josslab.redis.jembedded.fi;

@FunctionalInterface
public interface CheckedRunnable {

  void run() throws Exception;
}
