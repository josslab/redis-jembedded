package io.josslab.redis.jembedded.fi;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier<T> {

  T get() throws IOException;

}
