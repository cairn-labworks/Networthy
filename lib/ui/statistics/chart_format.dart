/// Percentage label shown under the donut's center value: whole numbers at or
/// above 10%, one decimal below that.
String percentageLabel(double value, double total) {
  if (total <= 0.0) return '';
  final double pct = value / total * 100.0;
  return pct >= 10.0 ? '${pct.truncate()}%' : '${pct.toStringAsFixed(1)}%';
}
