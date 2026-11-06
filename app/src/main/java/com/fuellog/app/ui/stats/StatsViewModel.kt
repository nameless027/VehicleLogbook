package com.fuellog.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuellog.app.AppContainer
import com.fuellog.app.data.repository.FuelRepository
import com.fuellog.app.data.repository.ServiceRepository
import com.fuellog.app.util.ConsumptionCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val odometer: Int? = null,
    val averageConsumption: Double? = null,
    val fuelTotal: Double = 0.0,
    val serviceTotal: Double = 0.0
) {
    val grandTotal: Double get() = fuelTotal + serviceTotal
}

class StatsViewModel(
    fuelRepository: FuelRepository,
    serviceRepository: ServiceRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        fuelRepository.observeRecords(),
        fuelRepository.observeTotalCost(),
        fuelRepository.observeMaxOdometer(),
        serviceRepository.observeTotalCost(),
        serviceRepository.observeMaxOdometer()
    ) { fuelRecords, fuelTotal, fuelMaxOdometer, serviceTotal, serviceMaxOdometer ->
        StatsUiState(
            odometer = listOfNotNull(fuelMaxOdometer, serviceMaxOdometer).maxOrNull(),
            averageConsumption = ConsumptionCalculator.averageConsumption(fuelRecords),
            fuelTotal = fuelTotal,
            serviceTotal = serviceTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StatsViewModel(container.fuelRepository, container.serviceRepository) as T
            }
    }
}