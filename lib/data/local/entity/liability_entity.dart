import '../../../domain/model/liability_type.dart';

/// Something owed, tracked inside a portfolio.
class LiabilityEntity {
  const LiabilityEntity({
    this.id = 0,
    required this.portfolioId,
    required this.type,
    required this.name,
    required this.currency,
    required this.amount,
    this.notes,
    this.position = 0,
    required this.createdAt,
    required this.updatedAt,
  });

  factory LiabilityEntity.fromMap(Map<String, Object?> map) => LiabilityEntity(
    id: map['id'] as int,
    portfolioId: map['portfolioId'] as int,
    type: LiabilityType.fromName(map['type'] as String?),
    name: map['name'] as String,
    currency: map['currency'] as String,
    amount: (map['amount'] as num).toDouble(),
    notes: map['notes'] as String?,
    position: map['position'] as int? ?? 0,
    createdAt: map['createdAt'] as int,
    updatedAt: map['updatedAt'] as int,
  );

  final int id;
  final int portfolioId;
  final LiabilityType type;
  final String name;
  final String currency;

  /// Outstanding amount owed, in [currency].
  final double amount;
  final String? notes;

  /// User-defined sort order within this liability's type. Lower shows first.
  final int position;
  final int createdAt;
  final int updatedAt;

  double get value => amount;

  Map<String, Object?> toMap({bool includeId = true}) => <String, Object?>{
    if (includeId && id != 0) 'id': id,
    'portfolioId': portfolioId,
    'type': type.storageName,
    'name': name,
    'currency': currency,
    'amount': amount,
    'notes': notes,
    'position': position,
    'createdAt': createdAt,
    'updatedAt': updatedAt,
  };

  LiabilityEntity copyWith({
    int? id,
    int? portfolioId,
    LiabilityType? type,
    String? name,
    String? currency,
    double? amount,
    String? notes,
    int? position,
    int? createdAt,
    int? updatedAt,
  }) => LiabilityEntity(
    id: id ?? this.id,
    portfolioId: portfolioId ?? this.portfolioId,
    type: type ?? this.type,
    name: name ?? this.name,
    currency: currency ?? this.currency,
    amount: amount ?? this.amount,
    notes: notes ?? this.notes,
    position: position ?? this.position,
    createdAt: createdAt ?? this.createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
  );
}
