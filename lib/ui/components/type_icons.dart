import 'package:flutter/material.dart';

import '../../domain/model/asset_type.dart';
import '../../domain/model/liability_type.dart';

IconData assetTypeIcon(AssetType type) {
  switch (type) {
    case AssetType.stock:
      return Icons.show_chart;
    case AssetType.mutualFund:
      return Icons.pie_chart;
    case AssetType.crypto:
      return Icons.currency_bitcoin;
    case AssetType.gold:
      return Icons.diamond;
    case AssetType.cash:
      return Icons.payments;
    case AssetType.bankDeposit:
      return Icons.account_balance;
    case AssetType.realEstate:
      return Icons.home;
    case AssetType.vehicle:
      return Icons.directions_car;
    case AssetType.bond:
      return Icons.description;
    case AssetType.other:
      return Icons.category;
  }
}

IconData liabilityTypeIcon(LiabilityType type) {
  switch (type) {
    case LiabilityType.loan:
      return Icons.account_balance_wallet;
    case LiabilityType.mortgage:
      return Icons.home_work;
    case LiabilityType.creditCard:
      return Icons.credit_card;
    case LiabilityType.pendingPayment:
      return Icons.schedule;
    case LiabilityType.tax:
      return Icons.receipt_long;
    case LiabilityType.other:
      return Icons.request_quote;
  }
}
