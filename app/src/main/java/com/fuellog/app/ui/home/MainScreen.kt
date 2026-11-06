package com.fuellog.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fuellog.app.FuelLogApplication
import com.fuellog.app.BuildConfig
import com.fuellog.app.R
import com.fuellog.app.ui.fuel.FuelListContent
import com.fuellog.app.ui.fuel.FuelListViewModel
import com.fuellog.app.ui.service.ServiceListContent
import com.fuellog.app.ui.service.ServiceListViewModel
import com.fuellog.app.ui.theme.AccentGreen
import com.fuellog.app.ui.theme.BackgroundBlack
import com.fuellog.app.ui.theme.CardDark
import com.fuellog.app.ui.theme.GreenTint
import com.fuellog.app.ui.theme.OnGreenDark
import com.fuellog.app.ui.theme.SurfaceDark
import com.fuellog.app.ui.theme.TextPrimaryDark
import com.fuellog.app.ui.theme.TextSecondaryDark
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenStats: () -> Unit,
    onOpenCar: () -> Unit,
    onAddFuel: () -> Unit,
    onAddService: () -> Unit,
    onFuelRecordClick: (Long) -> Unit,
    onServiceRecordClick: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as FuelLogApplication
    val fuelViewModel: FuelListViewModel =
        viewModel(factory = FuelListViewModel.factory(app.container))
    val serviceViewModel: ServiceListViewModel =
        viewModel(factory = ServiceListViewModel.factory(app.container))

    val fuelRecords by fuelViewModel.records.collectAsStateWithLifecycle()
    val serviceRecords by serviceViewModel.records.collectAsStateWithLifecycle()
    val carProfile by app.container.carStore.profile.collectAsStateWithLifecycle()

    val carName = listOf(carProfile.make, carProfile.model)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    val title = carName.ifBlank { stringResource(R.string.app_name) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = AccentGreen)
                        Spacer(Modifier.width(10.dp))
                        Text(title)
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_menu))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(CardDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.car_title)) },
                            leadingIcon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenCar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_statistics)) },
                            leadingIcon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenStats()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about_title)) },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showAboutDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_support)) },
                            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showSupportDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack,
                    titleContentColor = TextPrimaryDark,
                    actionIconContentColor = TextSecondaryDark
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.LocalGasStation, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_fuel)) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Build, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_service)) },
                    colors = navigationItemColors()
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = if (selectedTab == 0) onAddFuel else onAddService,
                containerColor = AccentGreen,
                contentColor = OnGreenDark
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> FuelListContent(records = fuelRecords, onRecordClick = onFuelRecordClick)
                else -> ServiceListContent(records = serviceRecords, onRecordClick = onServiceRecordClick)
            }
        }
    }
    // Диалог «Сведения»
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.about_app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.about_description))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.about_developed),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
    // Диалог «Поддержать»
    if (showSupportDialog) {
        val uriHandler = LocalUriHandler.current
        val supportLink = stringResource(R.string.support_link)
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text(stringResource(R.string.support_title), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.support_message),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.support_link_label),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        supportLink,
                        modifier = Modifier.clickable {
                            uriHandler.openUri(supportLink)
                        },
                        color = AccentGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Image(
                        painter = painterResource(R.drawable.qr_support),
                        contentDescription = stringResource(R.string.cd_support_qr),
                        modifier = Modifier.size(200.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AccentGreen,
    selectedTextColor = AccentGreen,
    indicatorColor = GreenTint,
    unselectedIconColor = TextSecondaryDark,
    unselectedTextColor = TextSecondaryDark
)