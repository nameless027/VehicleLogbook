package com.fuellog.app.ui.fuel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuellog.app.AppContainer
import com.fuellog.app.R
import com.fuellog.app.data.local.entity.FuelRecordEntity
import com.fuellog.app.data.repository.FuelFormInput
import com.fuellog.app.data.repository.FuelRepository
import com.fuellog.app.util.FuelTypes
import com.fuellog.app.util.Fmt
import com.fuellog.app.util.parseDoubleFlexible
import com.fuellog.app.util.sanitizeDecimalInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Режим ввода стоимости: цена за литр или готовая сумма заправки. */
enum class PriceMode { PER_LITER, TOTAL }

data class FuelFormState(
    val isLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val odometerText: String = "",
    val fuelType: String = FuelTypes.AI_95,
    val litersText: String = "",
    val priceText: String = "",
    val priceMode: PriceMode = PriceMode.PER_LITER,
    val fullTank: Boolean = false,
    val odometerError: String? = null,
    val litersError: String? = null,
    val priceError: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false
)

class FuelFormViewModel(
    private val repository: FuelRepository,
    context: Context,
    private val recordId: Long?
) : ViewModel() {

    private val appContext: Context = context.applicationContext

    private val _state = MutableStateFlow(FuelFormState(isLoading = recordId != null))
    val state: StateFlow<FuelFormState> = _state.asStateFlow()

    private var existing: FuelRecordEntity? = null

    /**
     * Живой пересчёт итога:
     * PER_LITER — литры × цена за литр;
     * TOTAL — введённая сумма заправки.
     */
    val totalCost: StateFlow<Double?> = _state
        .map { s ->
            val liters = s.litersText.parseDoubleFlexible()
            val entered = s.priceText.parseDoubleFlexible()
            when (s.priceMode) {
                PriceMode.PER_LITER ->
                    if (liters != null && entered != null && liters > 0 && entered >= 0) liters * entered else null
                PriceMode.TOTAL ->
                    if (entered != null && entered > 0) entered else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        if (recordId != null) {
            viewModelScope.launch {
                val record = repository.getRecord(recordId)
                if (record == null) {
                    _state.update {
                        it.copy(isLoading = false, formError = appContext.getString(R.string.error_record_not_found))
                    }
                } else {
                    existing = record
                    _state.update {
                        it.copy(
                            isLoading = false,
                            date = record.date,
                            odometerText = record.odometer.toString(),
                            fuelType = record.fuelType,
                            litersText = Fmt.plain(record.liters),
                            priceText = Fmt.plain(record.pricePerLiter),
                            priceMode = PriceMode.PER_LITER,
                            fullTank = record.fullTank
                        )
                    }
                }
            }
        }
    }

    fun onDateChange(date: LocalDate) = _state.update { it.copy(date = date) }

    fun onOdometerChange(value: String) = _state.update {
        it.copy(odometerText = value.filter(Char::isDigit), odometerError = null)
    }

    fun onFuelTypeChange(value: String) = _state.update { it.copy(fuelType = value) }

    fun onLitersChange(value: String) = _state.update {
        it.copy(litersText = sanitizeDecimalInput(value), litersError = null)
    }

    fun onPriceChange(value: String) = _state.update {
        it.copy(priceText = sanitizeDecimalInput(value), priceError = null)
    }

    fun onPriceModeChange(mode: PriceMode) = _state.update {
        it.copy(priceMode = mode, priceError = null)
    }

    fun onFullTankChange(value: Boolean) = _state.update { it.copy(fullTank = value) }

    fun save(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.isSaving || s.isLoading) return

        val odometer = s.odometerText.parseDoubleFlexible()?.takeIf { it > 0 }?.toInt()
        val liters = s.litersText.parseDoubleFlexible()?.takeIf { it > 0 }
        val entered = s.priceText.parseDoubleFlexible()

        // В режиме TOTAL цена за литр вычисляется как сумма / литры
        val pricePerLiter: Double? = when (s.priceMode) {
            PriceMode.PER_LITER -> entered?.takeIf { it >= 0 }
            PriceMode.TOTAL ->
                if (entered != null && entered > 0 && liters != null) entered / liters else null
        }

        val odometerError =
            if (odometer == null) appContext.getString(R.string.error_odometer) else null
        val litersError =
            if (liters == null) appContext.getString(R.string.error_liters) else null
        val priceError = if (pricePerLiter == null) {
            appContext.getString(
                if (s.priceMode == PriceMode.PER_LITER) R.string.error_price else R.string.error_total
            )
        } else null

        _state.update {
            it.copy(
                odometerError = odometerError,
                litersError = litersError,
                priceError = priceError,
                formError = null
            )
        }
        if (odometer == null || liters == null || pricePerLiter == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                repository.save(
                    input = FuelFormInput(
                        date = s.date,
                        odometer = odometer,
                        fuelType = s.fuelType,
                        liters = liters,
                        pricePerLiter = pricePerLiter,
                        fullTank = s.fullTank
                    ),
                    existing = existing
                )
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(isSaving = false, formError = appContext.getString(R.string.error_save_failed))
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, context: Context, recordId: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FuelFormViewModel(container.fuelRepository, context, recordId) as T
            }
    }
}