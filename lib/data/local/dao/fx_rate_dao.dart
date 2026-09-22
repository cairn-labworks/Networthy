import 'package:sqflite_sqlcipher/sqflite.dart';

import '../app_database.dart';
import '../entity/fx_rate_entity.dart';

class FxRateDao {
  const FxRateDao(this._database);

  final AppDatabase _database;

  Database get _db => _database.db;

  Stream<List<FxRateEntity>> observeAll() =>
      _database.watch(<String>{Tables.fxRates}, getAll);

  Future<List<FxRateEntity>> getAll() async {
    final List<Map<String, Object?>> rows = await _db.query(Tables.fxRates);
    return rows.map(FxRateEntity.fromMap).toList(growable: false);
  }

  Future<FxRateEntity?> getByCurrency(String currency) async {
    final List<Map<String, Object?>> rows = await _db.query(
      Tables.fxRates,
      where: 'currency = ?',
      whereArgs: <Object?>[currency],
    );
    return rows.isEmpty ? null : FxRateEntity.fromMap(rows.first);
  }

  Future<void> upsertAll(List<FxRateEntity> rates) async {
    await _db.transaction((Transaction txn) async {
      for (final FxRateEntity rate in rates) {
        await txn.insert(
          Tables.fxRates,
          rate.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      }
    });
    _database.notify(<String>{Tables.fxRates});
  }

  Future<int?> latestTimestamp() async => Sqflite.firstIntValue(
    await _db.rawQuery('SELECT MAX(timestamp) FROM ${Tables.fxRates}'),
  );
}
