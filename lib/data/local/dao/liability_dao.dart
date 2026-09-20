import 'package:sqflite_sqlcipher/sqflite.dart';

import '../../../domain/model/liability_type.dart';
import '../app_database.dart';
import '../entity/liability_entity.dart';

class LiabilityDao {
  const LiabilityDao(this._database);

  final AppDatabase _database;

  Database get _db => _database.db;

  Stream<List<LiabilityEntity>> observeForPortfolio(int portfolioId) =>
      _database.watch(<String>{
        Tables.liabilities,
      }, () => getForPortfolioOrdered(portfolioId));

  Stream<LiabilityEntity?> observeById(int id) =>
      _database.watch(<String>{Tables.liabilities}, () => getById(id));

  Future<List<LiabilityEntity>> getForPortfolioOrdered(int portfolioId) async {
    final List<Map<String, Object?>> rows = await _db.query(
      Tables.liabilities,
      where: 'portfolioId = ?',
      whereArgs: <Object?>[portfolioId],
      orderBy: 'position ASC, updatedAt DESC',
    );
    return rows.map(LiabilityEntity.fromMap).toList(growable: false);
  }

  Future<List<LiabilityEntity>> getForPortfolio(int portfolioId) async {
    final List<Map<String, Object?>> rows = await _db.query(
      Tables.liabilities,
      where: 'portfolioId = ?',
      whereArgs: <Object?>[portfolioId],
    );
    return rows.map(LiabilityEntity.fromMap).toList(growable: false);
  }

  Future<LiabilityEntity?> getById(int id) async {
    final List<Map<String, Object?>> rows = await _db.query(
      Tables.liabilities,
      where: 'id = ?',
      whereArgs: <Object?>[id],
    );
    return rows.isEmpty ? null : LiabilityEntity.fromMap(rows.first);
  }

  Future<int> insert(LiabilityEntity liability) async {
    final int id = await _db.insert(
      Tables.liabilities,
      liability.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
    _notify();
    return id;
  }

  Future<void> update(LiabilityEntity liability) async {
    await _db.update(
      Tables.liabilities,
      liability.toMap(),
      where: 'id = ?',
      whereArgs: <Object?>[liability.id],
    );
    _notify();
  }

  Future<void> deleteById(int id) async {
    await _db.delete(
      Tables.liabilities,
      where: 'id = ?',
      whereArgs: <Object?>[id],
    );
    _notify();
  }

  Future<int> maxPosition(int portfolioId, LiabilityType type) async =>
      Sqflite.firstIntValue(
        await _db.rawQuery(
          'SELECT COALESCE(MAX(position), -1) FROM ${Tables.liabilities} '
          'WHERE portfolioId = ? AND type = ?',
          <Object?>[portfolioId, type.storageName],
        ),
      ) ??
      -1;

  /// Persists a new order for the given liability ids (index becomes position).
  Future<void> updatePositions(List<int> orderedIds) async {
    await _db.transaction((Transaction txn) async {
      for (int index = 0; index < orderedIds.length; index++) {
        await txn.rawUpdate(
          'UPDATE ${Tables.liabilities} SET position = ? WHERE id = ?',
          <Object?>[index, orderedIds[index]],
        );
      }
    });
    _notify();
  }

  void _notify() => _database.notify(<String>{Tables.liabilities});
}
