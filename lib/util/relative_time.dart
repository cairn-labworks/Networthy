import 'package:intl/intl.dart';

/// Human-friendly "time ago" label for the exchange-rate timestamp.
///
/// Mirrors Android's `DateUtils.getRelativeTimeSpanString` closely enough for
/// the settings row: minute resolution, "Yesterday" for one day, then a short
/// date once the timestamp is more than a week old.
String relativeTimeSpan(int epochMillis, {DateTime? now}) {
  final DateTime moment = DateTime.fromMillisecondsSinceEpoch(epochMillis);
  final DateTime reference = now ?? DateTime.now();
  final int deltaMillis = reference.millisecondsSinceEpoch - epochMillis;
  if (deltaMillis < 0) return DateFormat.yMMMd().format(moment);

  const int minute = 60 * 1000;
  const int hour = 60 * minute;
  const int day = 24 * hour;

  if (deltaMillis < minute) return '0 minutes ago';
  if (deltaMillis < hour) {
    final int minutes = deltaMillis ~/ minute;
    return minutes == 1 ? '1 minute ago' : '$minutes minutes ago';
  }
  if (deltaMillis < day) {
    final int hours = deltaMillis ~/ hour;
    return hours == 1 ? '1 hour ago' : '$hours hours ago';
  }
  final int days = deltaMillis ~/ day;
  if (days == 1) return 'Yesterday';
  if (days < 7) return '$days days ago';
  return DateFormat.yMMMd().format(moment);
}
