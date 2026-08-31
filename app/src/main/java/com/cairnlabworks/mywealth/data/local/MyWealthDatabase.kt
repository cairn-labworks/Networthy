package com.cairnlabworks.mywealth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cairnlabworks.mywealth.data.local.dao.AssetDao
import com.cairnlabworks.mywealth.data.local.dao.FxRateDao
import com.cairnlabworks.mywealth.data.local.dao.LiabilityDao
import com.cairnlabworks.mywealth.data.local.dao.PortfolioDao
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.FxRateEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        PortfolioEntity::class,
        AssetEntity::class,
        LiabilityEntity::class,
        FxRateEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MyWealthDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao
    abstract fun assetDao(): AssetDao
    abstract fun liabilityDao(): LiabilityDao
    abstract fun fxRateDao(): FxRateDao

    companion object {
        private const val DATABASE_NAME = "mywealth.db"

        /** Adds the custom-sort `position` column to assets and liabilities. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assets ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE liabilities ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Builds the encrypted database. The [passphrase] is consumed (zeroed) by
         * SQLCipher, so callers must pass a disposable copy.
         */
        fun create(context: Context, passphrase: ByteArray): MyWealthDatabase {
            SQLiteDatabase.loadLibs(context)
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                MyWealthDatabase::class.java,
                DATABASE_NAME,
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
