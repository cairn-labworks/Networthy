/// Returns a new list sorted by [selector] in descending order.
///
/// Kotlin's `sortedByDescending` is a stable sort, so items with equal amounts
/// keep their original relative order. Dart's [List.sort] is not stable, hence
/// the explicit index tie-breaker.
List<T> sortedByDescending<T>(Iterable<T> items, num Function(T) selector) {
  final List<T> source = items.toList(growable: false);
  final List<int> indices = List<int>.generate(source.length, (int i) => i);
  indices.sort((int a, int b) {
    final int byValue = selector(source[b]).compareTo(selector(source[a]));
    if (byValue != 0) return byValue;
    return a.compareTo(b);
  });
  return <T>[for (final int i in indices) source[i]];
}

/// Groups [items] by [keyOf], preserving first-encounter order of the keys and
/// the original order of the values, like Kotlin's `groupBy`.
Map<K, List<V>> groupBy<K, V>(Iterable<V> items, K Function(V) keyOf) {
  final Map<K, List<V>> grouped = <K, List<V>>{};
  for (final V item in items) {
    grouped.putIfAbsent(keyOf(item), () => <V>[]).add(item);
  }
  return grouped;
}
