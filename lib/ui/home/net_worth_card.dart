import 'package:flutter/material.dart';

import '../../domain/model/net_worth_summary.dart';
import '../../util/currency_util.dart';
import '../theme/theme.dart';

/// The headline card: net worth plus the asset and liability totals.
class NetWorthCard extends StatelessWidget {
  const NetWorthCard({
    required this.summary,
    required this.hideBalances,
    super.key,
  });

  final NetWorthSummary summary;
  final bool hideBalances;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final FinanceColors finance = FinanceColors.of(context);
    return Card(
      color: theme.colorScheme.primaryContainer,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(shapeExtraLarge),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Text(
                  'Net worth',
                  style: theme.textTheme.labelLarge?.copyWith(
                    color: theme.colorScheme.onPrimaryContainer,
                  ),
                ),
                if (summary.hasApproximateConversions) ...<Widget>[
                  const SizedBox(width: 8),
                  Tooltip(
                    message: 'Some values are approximate',
                    child: Icon(
                      Icons.info_outlined,
                      size: 16,
                      color: theme.colorScheme.onPrimaryContainer,
                    ),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 6),
            Text(
              hideBalances
                  ? CurrencyUtil.masked(summary.baseCurrency)
                  : CurrencyUtil.format(summary.netWorth, summary.baseCurrency),
              style: theme.textTheme.displaySmall?.copyWith(
                fontWeight: FontWeight.w600,
                color: theme.colorScheme.onPrimaryContainer,
              ),
            ),
            const SizedBox(height: 20),
            Row(
              children: <Widget>[
                Expanded(
                  child: _MetricTile(
                    icon: Icons.trending_up,
                    accent: finance.positive,
                    label: 'Assets',
                    amount: hideBalances
                        ? CurrencyUtil.maskShort
                        : CurrencyUtil.format(
                            summary.totalAssets,
                            summary.baseCurrency,
                          ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _MetricTile(
                    icon: Icons.trending_down,
                    accent: finance.negative,
                    label: 'Liabilities',
                    amount: hideBalances
                        ? CurrencyUtil.maskShort
                        : CurrencyUtil.format(
                            summary.totalLiabilities,
                            summary.baseCurrency,
                          ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricTile extends StatelessWidget {
  const _MetricTile({
    required this.icon,
    required this.accent,
    required this.label,
    required this.amount,
  });

  final IconData icon;
  final Color accent;
  final String label;
  final String amount;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    return Material(
      color: theme.colorScheme.surface,
      borderRadius: BorderRadius.circular(shapeLarge),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Icon(icon, size: 18, color: accent),
                const SizedBox(width: 6),
                Text(
                  label,
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            FittedBox(
              fit: BoxFit.scaleDown,
              alignment: Alignment.centerLeft,
              child: Text(
                amount,
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: theme.colorScheme.onSurface,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
