package com.fuellog.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "fuel_records")
data class FuelRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "odometer")
    val odometer: Int,
    @ColumnInfo(name = "fuel_type")
    val fuelType: String,
    @ColumnInfo(name = "liters")
    val liters: Double,
    @ColumnInfo(name = "price_per_liter")
    val pricePerLiter: Double,
    @ColumnInfo(name = "total_price")
    val totalPrice: Double,
    @ColumnInfo(name = "full_tank")
    val fullTank: Boolean,
    @ColumnInfo(name = "average_consumption")
    val averageConsumption: Double?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)