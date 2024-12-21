package io.josslab.redis.jembedded.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public final class BuilderFactory {

  private BuilderFactory() {
    // ignored
  }

  public interface Builder<T> {

    T build(Object data);
  }

  public static final Builder<Map<String, String>> STRING_MAP = data -> {
    final List<String> flatHash = (List<String>) data;
    final Map<String, String> hash = new HashMap<>(flatHash.size() / 2, 1);
    final Iterator<String> iterator = flatHash.iterator();
    while (iterator.hasNext()) {
      hash.put(iterator.next(), iterator.next());
    }

    return hash;
  };

}
