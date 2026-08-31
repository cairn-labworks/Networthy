package com.cairnlabworks.mywealth.data.backup

import com.cairnlabworks.mywealth.data.local.dao.AssetDao
import com.cairnlabworks.mywealth.data.local.dao.LiabilityDao
import com.cairnlabworks.mywealth.data.local.dao.PortfolioDao
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.data.security.BackupCrypto
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.LiabilityType
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exports and imports portfolios as password-encrypted files. Export gathers the
 * selected portfolios with all holdings; import re-creates them as new portfolios
 * so existing data is never overwritten.
 */
class BackupRepository(
    private val portfolioDao: PortfolioDao,
    private val assetDao: AssetDao,
    private val liabilityDao: LiabilityDao,
) {
    private val gson = Gson()

    suspend fun export(portfolioIds: List<Long>, password: CharArray): ByteArray =
        withContext(Dispatchers.IO) {
            val portfolios = portfolioIds.mapNotNull { id ->
                val portfolio = portfolioDao.getById(id) ?: return@mapNotNull null
                val assets = assetDao.getForPortfolio(id).map { it.toBackup() }
                val liabilities = liabilityDao.getForPortfolio(id).map { it.toBackup() }
                BackupPortfolio(
                    name = portfolio.name,
                    isDefault = portfolio.isDefault,
                    assets = assets,
                    liabilities = liabilities,
                )
            }
            val backup = BackupFile(
                exportedAt = System.currentTimeMillis(),
                portfolios = portfolios,
            )
            val json = gson.toJson(backup).toByteArray(Charsets.UTF_8)
            BackupCrypto.encrypt(json, password)
        }

    suspend fun import(data: ByteArray, password: CharArray): ImportResult =
        withContext(Dispatchers.IO) {
            val json = BackupCrypto.decrypt(data, password).toString(Charsets.UTF_8)
            val backup = runCatching { gson.fromJson(json, BackupFile::class.java) }.getOrNull()
                ?: throw BackupCrypto.InvalidBackupException("Backup file is not readable.")
            if (backup.portfolios == null) {
                throw BackupCrypto.InvalidBackupException("Backup file contains no portfolios.")
            }

            val existingNames = portfolioDao.getAll().map { it.name }.toMutableSet()
            var assetCount = 0
            var liabilityCount = 0

            for (portfolio in backup.portfolios) {
                val name = uniqueName(portfolio.name.ifBlank { "Imported portfolio" }, existingNames)
                existingNames += name
                val now = System.currentTimeMillis()
                val portfolioId = portfolioDao.insert(
                    PortfolioEntity(name = name, isDefault = false, createdAt = now, updatedAt = now),
                )
                portfolio.assets.orEmpty().forEach { asset ->
                    assetDao.insert(asset.toEntity(portfolioId))
                    assetCount++
                }
                portfolio.liabilities.orEmpty().forEach { liability ->
                    liabilityDao.insert(liability.toEntity(portfolioId))
                    liabilityCount++
                }
            }

            // Guarantee a default portfolio exists after import.
            if (portfolioDao.getDefault() == null) {
                portfolioDao.getAll().firstOrNull()?.let { portfolioDao.setDefault(it.id) }
            }

            ImportResult(
                portfolios = backup.portfolios.size,
                assets = assetCount,
                liabilities = liabilityCount,
            )
        }

    private fun uniqueName(base: String, taken: Set<String>): String {
        if (base !in taken) return base
        var i = 2
        while ("$base ($i)" in taken) i++
        return "$base ($i)"
    }
}

private fun AssetEntity.toBackup() = BackupAsset(
    type = type.name,
    name = name,
    currency = currency,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    symbol = symbol,
    lastPrice = lastPrice,
    lastPriceTimestamp = lastPriceTimestamp,
    manualValue = manualValue,
    notes = notes,
    position = position,
)

private fun LiabilityEntity.toBackup() = BackupLiability(
    type = type.name,
    name = name,
    currency = currency,
    amount = amount,
    notes = notes,
    position = position,
)

private fun BackupAsset.toEntity(portfolioId: Long) = AssetEntity(
    portfolioId = portfolioId,
    type = AssetType.fromName(type),
    name = name,
    currency = currency,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    symbol = symbol,
    lastPrice = lastPrice,
    lastPriceTimestamp = lastPriceTimestamp,
    manualValue = manualValue,
    notes = notes,
    position = position,
)

private fun BackupLiability.toEntity(portfolioId: Long) = LiabilityEntity(
    portfolioId = portfolioId,
    type = LiabilityType.fromName(type),
    name = name,
    currency = currency,
    amount = amount,
    notes = notes,
    position = position,
)
