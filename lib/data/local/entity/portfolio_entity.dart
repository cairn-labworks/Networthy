/// A named collection of assets and liabilities.
class PortfolioEntity {
  const PortfolioEntity({
    this.id = 0,
    required this.name,
    this.isDefault = false,
    required this.createdAt,
    required this.updatedAt,
  });

  factory PortfolioEntity.fromMap(Map<String, Object?> map) => PortfolioEntity(
    id: map['id'] as int,
    name: map['name'] as String,
    isDefault: (map['isDefault'] as int) != 0,
    createdAt: map['createdAt'] as int,
    updatedAt: map['updatedAt'] as int,
  );

  final int id;
  final String name;
  final bool isDefault;
  final int createdAt;
  final int updatedAt;

  Map<String, Object?> toMap({bool includeId = true}) => <String, Object?>{
    if (includeId && id != 0) 'id': id,
    'name': name,
    'isDefault': isDefault ? 1 : 0,
    'createdAt': createdAt,
    'updatedAt': updatedAt,
  };

  PortfolioEntity copyWith({
    int? id,
    String? name,
    bool? isDefault,
    int? createdAt,
    int? updatedAt,
  }) => PortfolioEntity(
    id: id ?? this.id,
    name: name ?? this.name,
    isDefault: isDefault ?? this.isDefault,
    createdAt: createdAt ?? this.createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
  );
}
