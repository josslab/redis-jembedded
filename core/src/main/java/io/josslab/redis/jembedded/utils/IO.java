package io.josslab.redis.jembedded.utils;

import io.josslab.redis.jembedded.fi.CheckedRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class IO {

  private IO() {
    // ignored
  }

  public static Runnable checkedToRuntime(final CheckedRunnable runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static void addShutdownHook(final String name, final Runnable run) {
    Runtime.getRuntime().addShutdownHook(new Thread(run, name));
  }

  public static void logStream(final InputStream stream, final Consumer<String> logConsumer) {
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logConsumer.accept(line);
      }
    } catch (final IOException ignored) {
      // ignored
    }
  }

  public static Thread newDaemonThread(final Runnable run) {
    final Thread thread = new Thread(run);
    thread.setDaemon(true);
    return thread;
  }

  public static boolean findMatchInStream(final InputStream in, final Pattern pattern,
                                          final Consumer<String> soutListener, final StringBuilder processOutput) throws IOException {
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (soutListener != null) soutListener.accept(line);
        processOutput.append('\n').append(line);
        if (pattern.matcher(line).matches())
          return true;
      }
    }
    return false;
  }

  public static String readFully(final InputStream in, final Consumer<String> listener) {
    final StringBuilder ret = new StringBuilder();
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (listener != null) listener.accept(line);
        ret.append(line);
      }
    } catch (final IOException ignored) {
      // ignored
    }
    return ret.toString();
  }

}
