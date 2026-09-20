import 'package:flutter/material.dart';

/// Builds one row of a [DragReorderColumn].
typedef DragReorderItemBuilder<T> = Widget Function(
  BuildContext context,
  T item,
  bool isDragging,
);

/// A column whose children can be reordered with a long press.
///
/// Mirrors the Compose `DragReorderColumn`: long-press an item to pick it up,
/// drop it to commit the new order. The callback receives the full reordered
/// list so callers can persist positions in one go.
class DragReorderColumn<T> extends StatelessWidget {
  const DragReorderColumn({
    required this.items,
    required this.keyOf,
    required this.onReordered,
    required this.itemBuilder,
    this.verticalSpacing = 0,
    this.padding = EdgeInsets.zero,
    super.key,
  });

  final List<T> items;
  final Object Function(T item) keyOf;
  final ValueChanged<List<T>> onReordered;
  final DragReorderItemBuilder<T> itemBuilder;
  final double verticalSpacing;
  final EdgeInsets padding;

  @override
  Widget build(BuildContext context) {
    return ReorderableListView.builder(
      shrinkWrap: true,
      padding: padding,
      physics: const NeverScrollableScrollPhysics(),
      buildDefaultDragHandles: false,
      itemCount: items.length,
      proxyDecorator: (Widget child, int index, Animation<double> animation) {
        return Material(
          type: MaterialType.transparency,
          child: itemBuilder(context, items[index], true),
        );
      },
      itemBuilder: (BuildContext context, int index) {
        final T item = items[index];
        return Padding(
          key: ValueKey<Object>(keyOf(item)),
          padding: EdgeInsets.only(
            bottom: index == items.length - 1 ? 0 : verticalSpacing,
          ),
          child: ReorderableDelayedDragStartListener(
            index: index,
            child: itemBuilder(context, item, false),
          ),
        );
      },
      onReorderItem: (int oldIndex, int newIndex) {
        if (newIndex == oldIndex) return;
        final List<T> reordered = List<T>.of(items);
        reordered.insert(newIndex, reordered.removeAt(oldIndex));
        onReordered(reordered);
      },
    );
  }
}
