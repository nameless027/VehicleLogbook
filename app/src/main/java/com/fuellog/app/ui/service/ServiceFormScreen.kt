package com.fuellog.app.ui.service

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fuellog.app.ui.theme.DeleteRed
import com.fuellog.app.ui.theme.GreenTint
import com.fuellog.app.ui.theme.GreenishText
import com.fuellog.app.ui.theme.OutlineDark
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import com.fuellog.app.util.Fmt
import com.fuellog.app.util.parseDoubleFlexible

@Composable
fun ServiceFormScreen(
    recordId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: ServiceFormViewModel =
        viewModel(factory = ServiceFormViewModel.factory(app.container, app, recordId))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val totalCost by viewModel.totalCost.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    // Автопрокрутка вниз при добавлении новой позиции
    val itemsCount = state.items.size
    var lastCount by remember { mutableIntStateOf(itemsCount) }
    LaunchedEffect(itemsCount) {
        if (itemsCount > lastCount) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        lastCount = itemsCount
    }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            AppTopBar(
                title = stringResource(if (recordId == null) R.string.service_add_title else R.string.service_edit_title),
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
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

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_notes)) },
                    placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    stringResource(R.string.parts_and_labor),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark
                )

                Spacer(Modifier.height(8.dp))

                if (state.items.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Text(
                            stringResource(R.string.no_items_yet),
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark
                        )
                    }
                } else {
                    state.items.forEachIndexed { index, item ->
                        ServiceItemEditor(
                            index = index,
                            item = item,
                            onRemove = { viewModel.removePart(item.key) },
                            onName = { value ->
                                viewModel.updatePart(item.key) { it.copy(name = value) }
                            },
                            onPartNumber = { value ->
                                viewModel.updatePart(item.key) { it.copy(partNumber = value) }
                            },
                            onQuantity = { value ->
                                viewModel.updatePart(item.key) { it.copy(quantityText = sanitizeItemNumber(value)) }
                            },
                            onUnitPrice = { value ->
                                viewModel.updatePart(item.key) { it.copy(unitPriceText = sanitizeItemNumber(value)) }
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // Кнопка добавления — ВСЕГДА под последней позицией
                Button(
                    onClick = viewModel::addPart,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenTint,
                        contentColor = AccentGreen
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.add_item))
                }

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
                            stringResource(R.string.total_service_cost),
                            style = MaterialTheme.typography.bodyLarge,
                            color = GreenishText
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            Fmt.money(totalCost),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AccentGreen
                        )
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

private fun sanitizeItemNumber(raw: String): String {
    val builder = StringBuilder(raw.length)
    var hasSeparator = false
    for (ch in raw) {
        when {
            ch.isDigit() -> builder.append(ch)
            (ch == '.' || ch == ',') && !hasSeparator -> {
                builder.append(ch)
                hasSeparator = true
            }
        }
    }
    return builder.toString()
}

@Composable
private fun ServiceItemEditor(
    index: Int,
    item: ServiceItemFormState,
    onRemove: () -> Unit,
    onName: (String) -> Unit,
    onPartNumber: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onUnitPrice: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, OutlineDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.item_index_format, index + 1),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentGreen
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_remove_item),
                        tint = DeleteRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = item.name,
                onValueChange = onName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_name)) },
                placeholder = { Text(stringResource(R.string.name_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = item.partNumber,
                onValueChange = onPartNumber,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_part_number)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppNumberField(
                    label = stringResource(R.string.label_qty),
                    value = item.quantityText,
                    onValueChange = onQuantity,
                    modifier = Modifier.weight(1f)
                )
                AppNumberField(
                    label = stringResource(R.string.label_unit_price),
                    suffix = "₽",
                    value = item.unitPriceText,
                    onValueChange = onUnitPrice,
                    modifier = Modifier.weight(1f)
                )
            }

            val qty = item.quantityText.parseDoubleFlexible()
            val price = item.unitPriceText.parseDoubleFlexible()
            if (qty != null && price != null && qty > 0 && price >= 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.item_total_format, Fmt.money(qty * price)),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AccentGreen
                )
            }
        }
    }
}