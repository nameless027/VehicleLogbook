package com.fuellog.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fuellog.app.ui.components.StatCard
import com.fuellog.app.ui.theme.BackgroundBlack
import com.fuellog.app.ui.theme.GreenTint
import com.fuellog.app.ui.theme.GreenishText
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.util.Fmt

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val viewModel: StatsViewModel = viewModel(factory = StatsViewModel.factory(app.container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = { AppTopBar(title = stringResource(R.string.stats_title), onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenTint)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.stats_odometer),
                        style = MaterialTheme.typography.labelLarge,
                        color = GreenishText
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            state.odometer?.let(Fmt::odometer) ?: stringResource(R.string.dash),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextPrimaryDark
                        )
                        if (state.odometer != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.unit_km),
                                style = MaterialTheme.typography.titleMedium,
                                color = GreenishText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }

            StatCard(
                icon = Icons.Outlined.Speed,
                label = stringResource(R.string.stats_avg_consumption),
                value = state.averageConsumption?.let(Fmt::consumption) ?: stringResource(R.string.dash)
            )
            StatCard(
                icon = Icons.Outlined.LocalGasStation,
                label = stringResource(R.string.stats_fuel_total),
                value = Fmt.money(state.fuelTotal)
            )
            StatCard(
                icon = Icons.Outlined.Build,
                label = stringResource(R.string.stats_service_total),
                value = Fmt.money(state.serviceTotal)
            )
            StatCard(
                icon = Icons.Outlined.Payments,
                label = stringResource(R.string.stats_grand_total),
                value = Fmt.money(state.grandTotal),
                emphasized = true
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}