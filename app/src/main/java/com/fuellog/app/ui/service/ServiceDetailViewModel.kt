package com.fuellog.app.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuellog.app.AppContainer
import com.fuellog.app.data.local.entity.ServiceItemEntity
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import com.fuellog.app.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServiceDetailUiState(
    val record: ServiceRecordEntity,
    val items: List<ServiceItemEntity>
)

class ServiceDetailViewModel(
    private val repository: ServiceRepository,
    private val recordId: Long
) : ViewModel() {

    val uiState: StateFlow<ServiceDetailUiState?> = combine(
        repository.observeRecord(recordId),
        repository.observeItems(recordId)
    ) { record, items ->
        record?.let { ServiceDetailUiState(it, items) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
                    ServiceDetailViewModel(container.serviceRepository, recordId) as T
            }
    }
}