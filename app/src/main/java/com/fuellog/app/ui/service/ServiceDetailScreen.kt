package com.fuellog.app.ui.service

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fuellog.app.FuelLogApplication
import com.fuellog.app.R
import com.fuellog.app.data.local.entity.ServiceItemEntity
import com.fuellog.app.ui.components.AppTopBar
import com.fuellog.app.ui.components.ConfirmDeleteDialog
import com.fuellog.app.ui.components.DetailRow
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.BackgroundBlack
import com.fuellog.app.ui.theme.CardDark
import com.fuellog.app.ui.theme.DeleteRed
import com.fuellog.app.ui.theme.OutlineDark
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import com.fuellog.app.util.Fmt

@Composable
fun ServiceDetailScreen(
    recordId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: ServiceDetailViewModel =
        viewModel(factory = ServiceDetailViewModel.factory(app.container, recordId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = { AppTopBar(title = stringResource(R.string.service_detail_title), onBack = onBack) }
    ) { padding ->
        val state = uiState
        if (state == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else {
            val record = state.record
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Fmt.date(record.date),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondaryDark
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                Fmt.odometer(record.odometer),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TextPrimaryDark
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.unit_km),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            Fmt.money(record.totalPrice),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentGreen
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        DetailRow(stringResource(R.string.label_date), Fmt.date(record.date))
                        DetailRow(
                            stringResource(R.string.label_odometer),
                            "${Fmt.odometer(record.odometer)} ${stringResource(R.string.unit_km)}"
                        )
                        DetailRow(
                            stringResource(R.string.label_total_cost),
                            Fmt.money(record.totalPrice),
                            valueColor = AccentGreen,
                            bold = true,
                            last = record.notes.isBlank()
                        )
                        if (record.notes.isNotBlank()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Text(
                                    stringResource(R.string.label_notes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondaryDark
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    record.notes,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimaryDark
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    stringResource(R.string.parts_and_labor_format, state.items.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark
                )

                Spacer(Modifier.height(8.dp))

                ItemsTable(items = state.items)

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEdit(record.id) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.edit))
                    }
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DeleteRed.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeleteRed)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            text = stringResource(R.string.service_delete_text),
            onConfirm = {
                confirmDelete = false
                viewModel.delete { onBack() }
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

private data class TableColumn(val title: String, val width: Dp, val alignEnd: Boolean = false)

@Composable
private fun tableColumns(): List<TableColumn> = listOf(
    TableColumn(stringResource(R.string.col_name), 190.dp),
    TableColumn(stringResource(R.string.col_part_number), 120.dp),
    TableColumn(stringResource(R.string.col_qty), 64.dp, alignEnd = true),
    TableColumn(stringResource(R.string.col_unit_price), 110.dp, alignEnd = true),
    TableColumn(stringResource(R.string.col_total), 110.dp, alignEnd = true)
)

@Composable
private fun ItemsTable(items: List<ServiceItemEntity>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ServiceItemCard(item)
        }
    }
}

@Composable
private fun ServiceItemCard(item: ServiceItemEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, OutlineDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Верхняя строка: название + итоговая сумма
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = Fmt.money(item.totalPrice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentGreen
                )
            }

            // Вторая строка: артикул (если есть), количество и цена — мелким серым
            val detailsParts = buildList {
                if (item.partNumber.isNotBlank()) {
                    add(item.partNumber)
                }
                add(Fmt.quantity(item.quantity))
                add("${Fmt.money(item.unitPrice)} / шт")
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = detailsParts.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
    }
}