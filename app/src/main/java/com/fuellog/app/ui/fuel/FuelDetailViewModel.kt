package com.fuellog.app.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuellog.app.AppContainer
import com.fuellog.app.data.local.entity.FuelRecordEntity
import com.fuellog.app.data.repository.FuelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FuelDetailViewModel(
    private val repository: FuelRepository,
    private val recordId: Long
) : ViewModel() {

    val record: StateFlow<FuelRecordEntity?> =
        repository.observeRecord(recordId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    fun delete(onDone: () -> Unit) {
        if (_isDeleting.value) return
        viewModelScope.launch {
            _isDeleting.value = true
            val current = repository.getRecord(recordId)
            if (current != null) {
                repository.delete(current)
            }
            onDone()
        }
    }

    companion object {
        fun factory(container: AppContainer, recordId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FuelDetailViewModel(container.fuelRepository, recordId) as T
            }
    }
}