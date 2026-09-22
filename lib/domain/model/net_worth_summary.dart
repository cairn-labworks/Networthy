/// Aggregated total for a single asset/liability category.
class CategoryTotal {
  const CategoryTotal({
    required this.typeName,
    required this.label,
    required this.amount,
    required this.count,
  });

  final String typeName;
  final String label;
  final double amount;
  final int count;
}

/// A portfolio's net worth expressed in a single base currency.
class NetWorthSummary {
  const NetWorthSummary({
    required this.baseCurrency,
    required this.totalAssets,
    required this.totalLiabilities,
    required this.assetBreakdown,
    required this.liabilityBreakdown,
    required this.hasApproximateConversions,
  });

  factory NetWorthSummary.empty(String baseCurrency) => NetWorthSummary(
    baseCurrency: baseCurrency,
    totalAssets: 0,
    totalLiabilities: 0,
    assetBreakdown: const <CategoryTotal>[],
    liabilityBreakdown: const <CategoryTotal>[],
    hasApproximateConversions: false,
  );

  final String baseCurrency;
  final double totalAssets;
  final double totalLiabilities;
  final List<CategoryTotal> assetBreakdown;
  final List<CategoryTotal> liabilityBreakdown;

  /// True if one or more items used a 1:1 fallback because no FX rate was cached.
  final bool hasApproximateConversions;

  double get netWorth => totalAssets - totalLiabilities;
}
