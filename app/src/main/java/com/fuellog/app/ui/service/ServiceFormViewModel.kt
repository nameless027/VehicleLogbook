package com.fuellog.app.ui.service

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuellog.app.AppContainer
import com.fuellog.app.R
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import com.fuellog.app.data.repository.ServiceFormInput
import com.fuellog.app.data.repository.ServiceItemInput
import com.fuellog.app.data.repository.ServiceRepository
import com.fuellog.app.util.Fmt
import com.fuellog.app.util.parseDoubleFlexible
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ServiceItemFormState(
    val key: Int,
    val name: String = "",
    val partNumber: String = "",
    val quantityText: String = "",
    val unitPriceText: String = ""
)

data class ServiceFormState(
    val isLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val odometerText: String = "",
    val notes: String = "",
    val items: List<ServiceItemFormState> = emptyList(),
    val odometerError: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false
)

class ServiceFormViewModel(
    private val repository: ServiceRepository,
    context: Context,
    private val recordId: Long?
) : ViewModel() {

    private val appContext: Context = context.applicationContext

    private val _state = MutableStateFlow(ServiceFormState(isLoading = recordId != null))
    val state: StateFlow<ServiceFormState> = _state.asStateFlow()

    private var existing: ServiceRecordEntity? = null
    private var itemKeyCounter = 0

    /** Итог = Σ (количество × цена) по всем позициям (живой пересчёт). */
    val totalCost: StateFlow<Double> = _state
        .map { s ->
            s.items.sumOf { item ->
                val qty = item.quantityText.parseDoubleFlexible()
                val price = item.unitPriceText.parseDoubleFlexible()
                if (qty != null && price != null && qty > 0 && price >= 0) qty * price else 0.0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

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
                    val loadedItems = repository.getItems(recordId).map { entity ->
                        ServiceItemFormState(
                            key = nextKey(),
                            name = entity.name,
                            partNumber = entity.partNumber,
                            quantityText = Fmt.plain(entity.quantity),
                            unitPriceText = Fmt.plain(entity.unitPrice)
                        )
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            date = record.date,
                            odometerText = record.odometer.toString(),
                            notes = record.notes,
                            items = loadedItems
                        )
                    }
                }
            }
        }
    }

    private fun nextKey(): Int = itemKeyCounter++

    fun onDateChange(date: LocalDate) = _state.update { it.copy(date = date) }

    fun onOdometerChange(value: String) = _state.update {
        it.copy(odometerText = value.filter(Char::isDigit), odometerError = null)
    }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun addPart() = _state.update {
        it.copy(items = it.items + ServiceItemFormState(key = nextKey()), formError = null)
    }

    fun removePart(key: Int) = _state.update {
        it.copy(items = it.items.filterNot { item -> item.key == key }, formError = null)
    }

    fun updatePart(key: Int, transform: (ServiceItemFormState) -> ServiceItemFormState) =
        _state.update { s ->
            s.copy(items = s.items.map { item -> if (item.key == key) transform(item) else item })
        }

    fun save(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.isSaving || s.isLoading) return

        val odometer = s.odometerText.parseDoubleFlexible()?.takeIf { it > 0 }?.toInt()
        if (odometer == null) {
            _state.update { it.copy(odometerError = appContext.getString(R.string.error_odometer)) }
            return
        }

        val parsed = ArrayList<ServiceItemInput>(s.items.size)
        for (item in s.items) {
            val isBlank = item.name.isBlank() && item.partNumber.isBlank() &&
                item.quantityText.isBlank() && item.unitPriceText.isBlank()
            if (isBlank) continue

            val qty = item.quantityText.parseDoubleFlexible()
            val price = item.unitPriceText.parseDoubleFlexible()
            if (item.name.isBlank() || qty == null || qty <= 0 || price == null || price < 0) {
                _state.update { it.copy(formError = appContext.getString(R.string.error_item_fields)) }
                return
            }
            parsed += ServiceItemInput(
                name = item.name.trim(),
                partNumber = item.partNumber.trim(),
                quantity = qty,
                unitPrice = price
            )
        }

        if (parsed.isEmpty()) {
            _state.update { it.copy(formError = appContext.getString(R.string.error_no_items)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, formError = null) }
            try {
                repository.save(
                    form = ServiceFormInput(date = s.date, odometer = odometer, notes = s.notes.trim()),
                    items = parsed,
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
                    ServiceFormViewModel(container.serviceRepository, context, recordId) as T
            }
    }
}