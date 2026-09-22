/// Cached foreign-exchange rate expressed as units of [currency] per 1 USD.
///
/// Used to convert asset/liability values into the user's base currency for
/// net-worth aggregation. USD itself is stored with unitsPerUsd = 1.0.
class FxRateEntity {
  const FxRateEntity({
    required this.currency,
    required this.unitsPerUsd,
    required this.timestamp,
  });

  factory FxRateEntity.fromMap(Map<String, Object?> map) => FxRateEntity(
    currency: map['currency'] as String,
    unitsPerUsd: (map['unitsPerUsd'] as num).toDouble(),
    timestamp: map['timestamp'] as int,
  );

  final String currency;
  final double unitsPerUsd;
  final int timestamp;

  Map<String, Object?> toMap() => <String, Object?>{
    'currency': currency,
    'unitsPerUsd': unitsPerUsd,
    'timestamp': timestamp,
  };
}
