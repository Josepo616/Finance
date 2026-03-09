package josealvarez.personal.finance.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import josealvarez.personal.finance.model.Budget
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onSave: (Budget) -> Unit,
    onBack: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Budget saved successfully")
            onSnackbarDismissed()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSnackbarDismissed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            BudgetForm(
                budget = uiState.budget,
                isSaving = uiState.isSaving,
                onSave = onSave
            )
        }
    }
}

@Composable
private fun BudgetForm(
    budget: Budget,
    isSaving: Boolean,
    onSave: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    var availableFunds by remember(budget) { mutableStateOf(budget.availableFunds.toFieldText()) }
    var dailyLimit by remember(budget) { mutableStateOf(budget.dailyLimit.toFieldText()) }
    var originalWeeklyLimit by remember(budget) { mutableStateOf(budget.originalWeeklyLimit.toFieldText()) }
    var monthlyLimit by remember(budget) { mutableStateOf(budget.monthlyLimit.toFieldText()) }
    
    var startDate by remember(budget) { mutableStateOf(budget.currentWeekStartDate) }
    var endDate by remember(budget) { mutableStateOf(budget.currentWeekEndDate) }

    var isFundsLocked by remember { mutableStateOf(true) }
    var isWeeklyLocked by remember { mutableStateOf(true) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoneyTextField(
                label = "Available Funds",
                value = availableFunds,
                onValueChange = { availableFunds = filterDecimalInput(it) },
                readOnly = isFundsLocked,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = !isFundsLocked, onCheckedChange = { isFundsLocked = !it })
                Text("Edit", style = MaterialTheme.typography.bodySmall)
            }
        }

        MoneyTextField(
            label = "Daily Limit",
            value = dailyLimit,
            onValueChange = { dailyLimit = filterDecimalInput(it) }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoneyTextField(
                label = "Weekly Limit (Original)",
                value = originalWeeklyLimit,
                onValueChange = { originalWeeklyLimit = filterDecimalInput(it) },
                readOnly = isWeeklyLocked,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = !isWeeklyLocked, onCheckedChange = { isWeeklyLocked = !it })
                Text("Edit", style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedButton(
            onClick = { showDateRangePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            val rangeText = if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                "$startDate to $endDate"
            } else {
                "Select Weekly Period"
            }
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(rangeText)
        }

        MoneyTextField(
            label = "Weekly Remaining",
            value = budget.currentWeeklyLimit.toFieldText(),
            onValueChange = {},
            readOnly = true
        )

        MoneyTextField(
            label = "Monthly Limit",
            value = monthlyLimit,
            onValueChange = { monthlyLimit = filterDecimalInput(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    budget.copy(
                        availableFunds = availableFunds.toDoubleOrNull() ?: 0.0,
                        dailyLimit = dailyLimit.toDoubleOrNull() ?: 0.0,
                        originalWeeklyLimit = originalWeeklyLimit.toDoubleOrNull() ?: 0.0,
                        monthlyLimit = monthlyLimit.toDoubleOrNull() ?: 0.0,
                        currentWeekStartDate = startDate,
                        currentWeekEndDate = endDate
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save")
        }
    }

    if (showDateRangePicker) {
        BudgetRangePicker(
            onDismiss = { showDateRangePicker = false },
            onRangeSelected = { start, end ->
                startDate = start
                endDate = end
                showDateRangePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetRangePicker(
    onDismiss: () -> Unit,
    onRangeSelected: (String, String) -> Unit
) {
    val state = rememberDateRangePickerState()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis?.let { sdf.format(Date(it)) }
                    val end = state.selectedEndDateMillis?.let { sdf.format(Date(it)) }
                    if (start != null && end != null) {
                        onRangeSelected(start, end)
                    }
                },
                enabled = state.selectedEndDateMillis != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = state,
            title = { Text("Select Weekly Budget Period", modifier = Modifier.padding(16.dp)) },
            headline = {
                val start = state.selectedStartDateMillis?.let { sdf.format(Date(it)) } ?: "Start Date"
                val end = state.selectedEndDateMillis?.let { sdf.format(Date(it)) } ?: "End Date"
                Text("$start - $end", modifier = Modifier.padding(16.dp))
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MoneyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        readOnly = readOnly,
        modifier = modifier.fillMaxWidth()
    )
}

private fun filterDecimalInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' }
    val parts = filtered.split(".")
    return when {
        parts.size <= 1 -> filtered
        parts.size >= 2 -> parts[0] + "." + parts[1].take(2)
        else -> filtered
    }
}

private fun Double.toFieldText(): String {
    return if (this == 0.0) "" else "%.2f".format(this)
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState(
                budget = Budget(
                    availableFunds = 1500.00,
                    dailyLimit = 50.00,
                    weeklyLimit = 200.00,
                    monthlyLimit = 800.00
                )
            ),
            onSave = {},
            onBack = {},
            onSnackbarDismissed = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
