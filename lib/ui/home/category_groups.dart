import '../../data/local/entity/asset_entity.dart';
import '../../data/local/entity/liability_entity.dart';
import '../../domain/model/asset_type.dart';
import '../../domain/model/liability_type.dart';

/// One collapsible asset category (all assets sharing a type) shown on Home.
class AssetCategoryGroup {
  const AssetCategoryGroup({
    required this.type,
    required this.total,
    required this.baseCurrency,
    required this.items,
  });

  final AssetType type;

  /// Total value of the group, converted to the base currency.
  final double total;
  final String baseCurrency;
  final List<AssetEntity> items;
}

/// One collapsible liability category (all liabilities sharing a type).
class LiabilityCategoryGroup {
  const LiabilityCategoryGroup({
    required this.type,
    required this.total,
    required this.baseCurrency,
    required this.items,
  });

  final LiabilityType type;
  final double total;
  final String baseCurrency;
  final List<LiabilityEntity> items;
}

/// The secondary line under an asset row: quantity, symbol and, when it differs
/// from the base currency, the asset's own currency.
String assetSubtitle(AssetEntity asset, String baseCurrency) {
  String core;
  switch (asset.type.valuationMode) {
    case ValuationMode.market:
      final String qty = asset.quantity == null
          ? '—'
          : formatQuantity(asset.quantity!);
      final String symbol = asset.symbol?.toUpperCase() ?? '';
      core = symbol.isNotEmpty ? '$qty × $symbol' : '$qty units';
    case ValuationMode.quantity:
      final String qty = asset.quantity == null
          ? '—'
          : formatQuantity(asset.quantity!);
      core = '$qty ${asset.type.unitLabel ?? ''}'.trim();
    case ValuationMode.flat:
      core = '';
  }
  return (core + currencyNote(asset.currency, baseCurrency)).trim();
}

String currencyNote(String currency, String baseCurrency) =>
    currency != baseCurrency ? '  ·  $currency' : '';

String formatQuantity(double value) =>
    value % 1.0 == 0.0 ? value.truncate().toString() : value.toString();

/// "1 item" / "N items".
String itemCountLabel(int count) => count == 1 ? '1 item' : '$count items';
