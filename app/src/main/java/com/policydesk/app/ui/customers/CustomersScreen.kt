package com.policydesk.app.ui.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.CustomerGroup
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.Navy800
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.Slate100
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate400
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600
import com.policydesk.app.ui.theme.StatusActiveGreen
import com.policydesk.app.ui.theme.StatusActiveGreenBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clients & Households",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Navy900
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text("Search client name, mobile, or vehicle...", fontSize = 13.sp, color = Slate400)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedIndicatorColor = SapphireBlue,
                    unfocusedIndicatorColor = Slate200
                )
            )

            // Header metric banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Clients", fontSize = 11.sp, color = Slate600)
                        Text("${uiState.totalCustomers}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Multi-Vehicle Clients", fontSize = 11.sp, color = Slate600)
                        Text("${uiState.multiVehicleCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SapphireBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Customer List
            if (uiState.customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No clients found matching query.", color = Slate600, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.customers, key = { it.mobile }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onNavigateToVehicle = onNavigateToVehicleDetail,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.mobile}"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                val formatted = if (customer.mobile.startsWith("91")) customer.mobile else "91${customer.mobile}"
                                val url = "https://wa.me/$formatted?text=Hello%20${Uri.encode(customer.name)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: CustomerGroup,
    onNavigateToVehicle: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Client Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SapphireBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val displayName = if (customer.title.isNullOrBlank()) customer.name else "${customer.title} ${customer.name}"
                        Text(text = displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        Text(text = customer.mobile, fontSize = 12.sp, color = Slate600)
                    }
                }

                // Vehicle count chip
                Box(
                    modifier = Modifier
                        .background(if (customer.vehicles.size > 1) SapphireBlue.copy(alpha = 0.12f) else Slate100, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${customer.vehicles.size} ${if (customer.vehicles.size == 1) "Vehicle" else "Vehicles"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.vehicles.size > 1) SapphireBlue else Navy800
                    )
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = customer.address, fontSize = 11.sp, color = Slate600, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row & Expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Vehicles" else "View Vehicles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SapphireBlue
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SapphireBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded Vehicles List
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customer.vehicles.forEach { item ->
                        val vehicle = item.vehicle
                        val policy = item.currentPolicy
                        val daysLeft = policy?.computeDaysRemaining() ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate50)
                                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                .clickable { onNavigateToVehicle(vehicle.regNo) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = RegistrationNormalizer.formatForDisplay(vehicle.regNo),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "${vehicle.makeModel ?: "Vehicle"} • ${policy?.insurer ?: "No Policy"}",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }

                            if (policy != null) {
                                DaysRemainingBadge(daysRemaining = daysLeft, status = policy.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
