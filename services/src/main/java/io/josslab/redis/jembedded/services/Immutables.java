package io.josslab.redis.jembedded.services;

import java.util.*;
import java.util.function.Supplier;

public final class Immutables {

  private Immutables() {
    // ignored
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <E, C extends Collection<E>> C unmodifiableCollection(Supplier<C> supplier, E... elements) {
    C collection = supplier.get();
    Collections.addAll(collection, elements);
    if (collection instanceof List<?>) {
      return (C) Collections.unmodifiableList((List<E>) collection);
    }
    if (collection instanceof NavigableSet<?>) {
      return (C) Collections.unmodifiableNavigableSet((NavigableSet<E>) collection);
    }
    if (collection instanceof SortedSet<?>) {
      return (C) Collections.unmodifiableSortedSet((SortedSet<E>) collection);
    }
    return (C) Collections.unmodifiableSet((Set<E>) collection);
  }
}
