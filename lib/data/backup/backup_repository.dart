import 'dart:convert';
import 'dart:typed_data';

import '../local/dao/asset_dao.dart';
import '../local/dao/liability_dao.dart';
import '../local/dao/portfolio_dao.dart';
import '../local/entity/asset_entity.dart';
import '../local/entity/liability_entity.dart';
import '../local/entity/portfolio_entity.dart';
import '../security/backup_crypto.dart';
import 'backup_mapper.dart';
import 'backup_models.dart';

/// Exports and imports portfolios as password-encrypted files. Export gathers the
/// selected portfolios with all holdings; import re-creates them as new portfolios
/// so existing data is never overwritten.
class BackupRepository {
  const BackupRepository(
    this._portfolioDao,
    this._assetDao,
    this._liabilityDao,
  );

  final PortfolioDao _portfolioDao;
  final AssetDao _assetDao;
  final LiabilityDao _liabilityDao;

  Future<Uint8List> export(List<int> portfolioIds, String password) async {
    final List<BackupPortfolio> portfolios = <BackupPortfolio>[];
    for (final int id in portfolioIds) {
      final PortfolioEntity? portfolio = await _portfolioDao.getById(id);
      if (portfolio == null) continue;
      final List<AssetEntity> assets = await _assetDao.getForPortfolio(id);
      final List<LiabilityEntity> liabilities = await _liabilityDao
          .getForPortfolio(id);
      portfolios.add(
        BackupPortfolio(
          name: portfolio.name,
          isDefault: portfolio.isDefault,
          assets: assets.map(assetToBackup).toList(growable: false),
          liabilities: liabilities
              .map(liabilityToBackup)
              .toList(growable: false),
        ),
      );
    }
    final BackupFile backup = BackupFile(
      exportedAt: DateTime.now().millisecondsSinceEpoch,
      portfolios: portfolios,
    );
    final Uint8List json = Uint8List.fromList(
      utf8.encode(jsonEncode(backup.toJson())),
    );
    return BackupCrypto.encrypt(json, password);
  }

  Future<ImportResult> import(Uint8List data, String password) async {
    final String json = utf8.decode(BackupCrypto.decrypt(data, password));
    BackupFile? backup;
    try {
      final Object? decoded = jsonDecode(json);
      if (decoded is Map<String, Object?>) {
        backup = BackupFile.fromJson(decoded);
      }
    } on FormatException {
      backup = null;
    }
    if (backup == null) {
      throw const InvalidBackupException('Backup file is not readable.');
    }
    final List<BackupPortfolio>? portfolios = backup.portfolios;
    if (portfolios == null) {
      throw const InvalidBackupException('Backup file contains no portfolios.');
    }

    final Set<String> existingNames = <String>{
      for (final PortfolioEntity portfolio in await _portfolioDao.getAll())
        portfolio.name,
    };
    int assetCount = 0;
    int liabilityCount = 0;

    for (final BackupPortfolio portfolio in portfolios) {
      final String name = uniqueName(
        portfolio.name.trim().isEmpty ? 'Imported portfolio' : portfolio.name,
        existingNames,
      );
      existingNames.add(name);
      final int now = DateTime.now().millisecondsSinceEpoch;
      final int portfolioId = await _portfolioDao.insert(
        PortfolioEntity(
          name: name,
          isDefault: false,
          createdAt: now,
          updatedAt: now,
        ),
      );
      for (final BackupAsset asset
          in portfolio.assets ?? const <BackupAsset>[]) {
        await _assetDao.insert(backupToAsset(asset, portfolioId, now));
        assetCount++;
      }
      for (final BackupLiability liability
          in portfolio.liabilities ?? const <BackupLiability>[]) {
        await _liabilityDao.insert(
          backupToLiability(liability, portfolioId, now),
        );
        liabilityCount++;
      }
    }

    // Guarantee a default portfolio exists after import.
    if (await _portfolioDao.getDefault() == null) {
      final List<PortfolioEntity> all = await _portfolioDao.getAll();
      if (all.isNotEmpty) await _portfolioDao.setDefault(all.first.id);
    }

    return ImportResult(
      portfolios: portfolios.length,
      assets: assetCount,
      liabilities: liabilityCount,
    );
  }
}
