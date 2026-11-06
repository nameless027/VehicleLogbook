package com.fuellog.app.ui.service

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fuellog.app.R
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import com.fuellog.app.ui.components.EmptyState
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.CardDark
import com.fuellog.app.ui.theme.OutlineDark
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import com.fuellog.app.util.Fmt

@Composable
fun ServiceListContent(
    records: List<ServiceRecordEntity>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Build,
            title = stringResource(R.string.service_empty_title),
            subtitle = stringResource(R.string.service_empty_subtitle),
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(records, key = { it.id }) { record ->
                ServiceRecordCard(record = record, onClick = { onRecordClick(record.id) })
            }
        }
    }
}

@Composable
private fun ServiceRecordCard(record: ServiceRecordEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, OutlineDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Fmt.date(record.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondaryDark
                )
                Spacer(Modifier.weight(1f))
                Text(
                    Fmt.money(record.totalPrice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentGreen
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    Fmt.odometer(record.odometer),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = TextPrimaryDark
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.unit_km),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}