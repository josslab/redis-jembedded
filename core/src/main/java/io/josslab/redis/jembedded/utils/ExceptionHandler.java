package io.josslab.redis.jembedded.utils;

import io.josslab.redis.jembedded.fi.IOSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Collections.unmodifiableMap;

public final class ExceptionHandler {

  private static final Map<String, BiFunction<String, Throwable, Throwable>> EXCEPTION_HANDLERS;

  static {
    Map<String, BiFunction<String, Throwable, Throwable>> exceptionMap = new HashMap<>();
    registerHandler(exceptionMap, IOException.class, UncheckedIOException::new);
    EXCEPTION_HANDLERS = unmodifiableMap(exceptionMap);
  }

  private ExceptionHandler() {
    // ignored
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable, R> R sneakyThrow(Throwable throwable) throws T {
    throw (T) throwable;
  }

  public static <T> T rethrow(Throwable throwable) {
    return rethrow(null, throwable);
  }

  public static <T> T rethrow(String message, Throwable throwable) {
    BiFunction<String, Throwable, Throwable> ex = EXCEPTION_HANDLERS.get(throwable.getClass().getName());
    if (ex == null) {
      return sneakyThrow(throwable);
    }
    return sneakyThrow(ex.apply(message, throwable));
  }

  public static <T> T supply(IOSupplier<T> ioSupplier) {
    try {
      return ioSupplier.get();
    } catch (IOException e) {
      return sneakyThrow(e);
    }
  }

  @SuppressWarnings({"unchecked"})
  private static <S extends Throwable, T extends Throwable> void registerHandler(
    Map<String, BiFunction<String, Throwable, Throwable>> exceptionMap,
    Class<S> sourceClass,
    BiFunction<String, S, T> targetFactory) {
    exceptionMap.put(sourceClass.getName(), (BiFunction<String, Throwable, Throwable>) targetFactory);
  }
}
