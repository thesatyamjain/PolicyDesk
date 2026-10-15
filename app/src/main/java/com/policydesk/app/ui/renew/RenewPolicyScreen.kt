package com.policydesk.app.ui.renew

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.addpolicy.FormSectionCard
import com.policydesk.app.ui.addpolicy.WarningBanner
import com.policydesk.app.ui.addpolicy.formDisabledFieldColors
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewPolicyScreen(
    onNavigateBack: () -> Unit,
    onRenewalComplete: (String) -> Unit,
    viewModel: RenewPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RenewUiEvent.RenewSuccess -> {
                    Toast.makeText(context, "Policy renewed successfully! New countdown active.", Toast.LENGTH_LONG).show()
                    onRenewalComplete(event.regNo)
                }
                is RenewUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.newExpiryEpochMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onExpiryDateSelected(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            com.policydesk.app.ui.components.LiquidGlassTopBar(
                title = "Renew Policy",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = com.policydesk.app.ui.theme.IosSystemBackground
    ) { paddingValues ->
        val item = state.currentVehicle
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading renewal form...", color = Slate600)
            }
        } else {
            val vehicle = item.vehicle
            val prevPolicy = item.currentPolicy

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vehicle Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF08A).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFCA8A04), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = RegistrationNormalizer.formatForDisplay(vehicle.regNo),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                            Text(
                                text = vehicle.customer.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                        }

                        if (prevPolicy != null) {
                            DetailRow(label = "Current Insurer", value = prevPolicy.insurer)
                            DetailRow(label = "Expiring / Expired On", value = UiFormatters.formatDate(prevPolicy.expiryDate))
                        }
                    }
                }

                // New Policy Details
                FormSectionCard(title = "New Policy Information") {
                    // New Policy Number
                    OutlinedTextField(
                        value = state.newPolicyNumber,
                        onValueChange = viewModel::onPolicyNumberChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Policy Number *") },
                        placeholder = { Text("Enter renewal policy number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    // New Expiry Date
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = state.newExpiryEpochMs?.let { UiFormatters.formatDate(it) } ?: "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            label = { Text("New Policy Expiry Date *") },
                            placeholder = { Text("Pick renewal expiry date") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = SapphireBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = formDisabledFieldColors()
                        )
                    }

                    if (state.earlierExpiryWarning) {
                        WarningBanner(
                            text = "Warning: The new expiry date is earlier than or equal to the previous expiry date."
                        )
                    }

                    // New Total Premium
                    OutlinedTextField(
                        value = state.newPremium,
                        onValueChange = viewModel::onPremiumChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Total Premium Cost (₹) *") },
                        placeholder = { Text("e.g. 14500") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    // Renewal Notes
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::onNotesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Renewal Notes") },
                        placeholder = { Text("Add-ons included, NCB discount, etc.") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::saveRenewal,
                    enabled = state.isValid && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SapphireBlue,
                        disabledContainerColor = Slate200
                    )
                ) {
                    Text(
                        text = if (state.isSaving) "Renewing..." else "Save Renewal & Start Countdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
