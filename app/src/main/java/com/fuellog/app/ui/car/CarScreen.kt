package com.fuellog.app.ui.car

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fuellog.app.AppContainer
import com.fuellog.app.FuelLogApplication
import com.fuellog.app.R
import com.fuellog.app.data.CarProfile
import com.fuellog.app.data.CarStore
import com.fuellog.app.ui.components.AppNumberField
import com.fuellog.app.ui.components.AppTopBar
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.BackgroundBlack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CarUiState(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = ""
)

class CarViewModel(private val store: CarStore) : ViewModel() {
    private val _state = MutableStateFlow(
        store.profile.value.let { p ->
            CarUiState(make = p.make, model = p.model, year = p.year, mileage = p.mileage)
        }
    )
    val state: StateFlow<CarUiState> = _state.asStateFlow()

    fun onMakeChange(v: String) = _state.update { it.copy(make = v) }
    fun onModelChange(v: String) = _state.update { it.copy(model = v) }
    fun onYearChange(v: String) = _state.update { it.copy(year = v.filter(Char::isDigit).take(4)) }
    fun onMileageChange(v: String) = _state.update { it.copy(mileage = v.filter(Char::isDigit)) }

    fun save(onSuccess: () -> Unit) {
        val s = _state.value
        store.save(
            CarProfile(
                make = s.make.trim(),
                model = s.model.trim(),
                year = s.year.trim(),
                mileage = s.mileage.trim()
            )
        )
        onSuccess()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CarViewModel(container.carStore) as T
            }
    }
}

@Composable
fun CarScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: CarViewModel = viewModel(factory = CarViewModel.factory(app.container))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = { AppTopBar(title = stringResource(R.string.car_title), onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()          // ← ЭТО ГЛАВНОЕ: задаёт конкретную высоту
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.make,
                onValueChange = viewModel::onMakeChange,
                label = { Text(stringResource(R.string.car_make)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.model,
                onValueChange = viewModel::onModelChange,
                label = { Text(stringResource(R.string.car_model)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                AppNumberField(
                    label = stringResource(R.string.car_year),
                    value = state.year,
                    onValueChange = viewModel::onYearChange,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                AppNumberField(
                    label = stringResource(R.string.car_mileage),
                    suffix = stringResource(R.string.unit_km),
                    value = state.mileage,
                    onValueChange = viewModel::onMileageChange,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.save(onBack) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.save))
            }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}