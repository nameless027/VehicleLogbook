package com.fuellog.app

import android.app.Application
import android.content.Context
import com.fuellog.app.data.CarStore
import com.fuellog.app.data.local.AppDatabase
import com.fuellog.app.data.repository.FuelRepository
import com.fuellog.app.data.repository.ServiceRepository

class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val fuelRepository: FuelRepository =
        FuelRepository(database.fuelRecordDao())

    val serviceRepository: ServiceRepository =
        ServiceRepository(database, database.serviceRecordDao(), database.serviceItemDao())

    val carStore: CarStore = CarStore(context)
}

class FuelLogApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}