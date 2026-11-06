package com.fuellog.app.ui.fuel

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fuellog.app.FuelLogApplication
import com.fuellog.app.R
import com.fuellog.app.ui.components.AppTopBar
import com.fuellog.app.ui.components.ConfirmDeleteDialog
import com.fuellog.app.ui.components.DetailRow
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.BackgroundBlack
import com.fuellog.app.ui.theme.CardDark
import com.fuellog.app.ui.theme.DeleteRed
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import com.fuellog.app.util.Fmt

@Composable
fun FuelDetailScreen(
    recordId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: FuelDetailViewModel =
        viewModel(factory = FuelDetailViewModel.factory(app.container, recordId))
    val record by viewModel.record.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = { AppTopBar(title = stringResource(R.string.fuel_detail_title), onBack = onBack) }
    ) { padding ->
        val current = record
        if (current == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else {
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
                            Fmt.date(current.date),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondaryDark
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                Fmt.odometer(current.odometer),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TextPrimaryDark
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.unit_km),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${current.fuelType} • ${Fmt.liters(current.liters)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryDark
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        DetailRow(stringResource(R.string.label_fuel_type), current.fuelType)
                        DetailRow(stringResource(R.string.label_liters), Fmt.liters(current.liters))
                        DetailRow(
                            stringResource(R.string.label_price_per_liter),
                            Fmt.pricePerLiter(current.pricePerLiter)
                        )
                        DetailRow(
                            stringResource(R.string.label_total_cost),
                            Fmt.money(current.totalPrice),
                            valueColor = AccentGreen,
                            bold = true
                        )
                        DetailRow(
                            stringResource(R.string.label_consumption),
                            current.averageConsumption?.let(Fmt::consumption) ?: stringResource(R.string.dash)
                        )
                        DetailRow(
                            stringResource(R.string.label_full_tank),
                            stringResource(if (current.fullTank) R.string.yes else R.string.no),
                            last = true
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEdit(current.id) },
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
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            text = stringResource(R.string.fuel_delete_text),
            onConfirm = {
                confirmDelete = false
                viewModel.delete { onBack() }
            },
            onDismiss = { confirmDelete = false }
        )
    }
}