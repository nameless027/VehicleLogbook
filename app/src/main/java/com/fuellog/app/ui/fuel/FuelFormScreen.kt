package com.fuellog.app.ui.fuel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fuellog.app.FuelLogApplication
import com.fuellog.app.R
import com.fuellog.app.ui.components.AppDateField
import com.fuellog.app.ui.components.AppNumberField
import com.fuellog.app.ui.components.AppTopBar
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.BackgroundBlack
import com.fuellog.app.ui.theme.CardDark
import com.fuellog.app.ui.theme.GreenTint
import com.fuellog.app.ui.theme.GreenishText
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import com.fuellog.app.util.Fmt
import com.fuellog.app.util.FuelTypes

@Composable
fun FuelFormScreen(
    recordId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: FuelFormViewModel =
        viewModel(factory = FuelFormViewModel.factory(app.container, app, recordId))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val totalCost by viewModel.totalCost.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            AppTopBar(
                title = stringResource(if (recordId == null) R.string.fuel_add_title else R.string.fuel_edit_title),
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            } else {
                Spacer(Modifier.height(8.dp))

                AppDateField(
                    value = state.date,
                    onDateSelected = viewModel::onDateChange,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                AppNumberField(
                    label = stringResource(R.string.label_odometer),
                    suffix = stringResource(R.string.unit_km),
                    value = state.odometerText,
                    onValueChange = viewModel::onOdometerChange,
                    error = state.odometerError,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                FuelTypeDropdown(selected = state.fuelType, onSelected = viewModel::onFuelTypeChange)

                Spacer(Modifier.height(12.dp))

                // Переключатель режима ввода стоимости
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriceModeChip(
                        selected = state.priceMode == PriceMode.PER_LITER,
                        label = stringResource(R.string.price_mode_per_liter),
                        onClick = { viewModel.onPriceModeChange(PriceMode.PER_LITER) },
                        modifier = Modifier.weight(1f)
                    )
                    PriceModeChip(
                        selected = state.priceMode == PriceMode.TOTAL,
                        label = stringResource(R.string.price_mode_total),
                        onClick = { viewModel.onPriceModeChange(PriceMode.TOTAL) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppNumberField(
                        label = stringResource(R.string.label_liters),
                        suffix = "л",
                        value = state.litersText,
                        onValueChange = viewModel::onLitersChange,
                        error = state.litersError,
                        modifier = Modifier.weight(1f)
                    )
                    AppNumberField(
                        label = stringResource(
                            if (state.priceMode == PriceMode.PER_LITER) {
                                R.string.label_price_per_liter
                            } else {
                                R.string.label_total_amount
                            }
                        ),
                        suffix = "₽",
                        value = state.priceText,
                        onValueChange = viewModel::onPriceChange,
                        error = state.priceError,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                FullTankSwitch(checked = state.fullTank, onCheckedChange = viewModel::onFullTankChange)

                val total = totalCost
                if (total != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenTint)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.label_total_cost),
                                style = MaterialTheme.typography.bodyLarge,
                                color = GreenishText
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                Fmt.money(total),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = AccentGreen
                            )
                        }
                    }
                }

                if (state.formError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        state.formError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.save(onSaved) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.Black
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PriceModeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenTint,
            selectedLabelColor = AccentGreen
        )
    )
}

@Composable
private fun FullTankSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.label_full_tank),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.full_tank_hint),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentGreen,
                uncheckedColor = TextSecondaryDark,
                checkmarkColor = Color.Black
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelTypeDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_fuel_type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FuelTypes.ALL.forEach { fuelType ->
                DropdownMenuItem(
                    text = { Text(fuelType) },
                    onClick = {
                        onSelected(fuelType)
                        expanded = false
                    }
                )
            }
        }
    }
}