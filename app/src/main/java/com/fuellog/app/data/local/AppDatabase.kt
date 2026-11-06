package com.fuellog.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fuellog.app.data.local.dao.FuelRecordDao
import com.fuellog.app.data.local.dao.ServiceItemDao
import com.fuellog.app.data.local.dao.ServiceRecordDao
import com.fuellog.app.data.local.dao.VehicleDao
import com.fuellog.app.data.local.entity.FuelRecordEntity
import com.fuellog.app.data.local.entity.ServiceItemEntity
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import com.fuellog.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        FuelRecordEntity::class,
        ServiceRecordEntity::class,
        ServiceItemEntity::class,
        VehicleEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fuelRecordDao(): FuelRecordDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun serviceItemDao(): ServiceItemDao
    abstract fun vehicleDao(): VehicleDao

    companion object {
        private const val DB_NAME = "vehicle_logbook.db"

        /** Миграция 1 → 2: добавление таблицы vehicle без потери данных. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `vehicle` (" +
                            "`id` INTEGER NOT NULL, " +
                            "`make` TEXT NOT NULL, " +
                            "`model` TEXT NOT NULL, " +
                            "`year` INTEGER, " +
                            "`odometer` INTEGER NOT NULL, " +
                            "`updated_at` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }
    }
}