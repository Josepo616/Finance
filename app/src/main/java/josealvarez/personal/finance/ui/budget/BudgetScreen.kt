package josealvarez.personal.finance.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import josealvarez.personal.finance.model.Budget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onSave: (Budget) -> Unit,
    onBack: () -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Limits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            BudgetForm(
                budget = uiState.budget,
                isSaving = uiState.isSaving,
                onSave = onSave,
                modifier = Modifier.padding(padding)
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
    var weeklyLimit by remember(budget) { mutableStateOf(budget.weeklyLimit.toFieldText()) }
    var monthlyLimit by remember(budget) { mutableStateOf(budget.monthlyLimit.toFieldText()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MoneyTextField(
            label = "Available Funds",
            value = availableFunds,
            onValueChange = { availableFunds = filterDecimalInput(it) }
        )

        MoneyTextField(
            label = "Daily Limit",
            value = dailyLimit,
            onValueChange = { dailyLimit = filterDecimalInput(it) }
        )

        MoneyTextField(
            label = "Weekly Limit",
            value = weeklyLimit,
            onValueChange = { weeklyLimit = filterDecimalInput(it) }
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
                    Budget(
                        availableFunds = availableFunds.toDoubleOrNull() ?: 0.0,
                        dailyLimit = dailyLimit.toDoubleOrNull() ?: 0.0,
                        weeklyLimit = weeklyLimit.toDoubleOrNull() ?: 0.0,
                        monthlyLimit = monthlyLimit.toDoubleOrNull() ?: 0.0
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
}

@Composable
private fun MoneyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun filterDecimalInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' }
    val parts = filtered.split(".")
    return when {
        parts.size <= 1 -> filtered
        else -> parts[0] + "." + parts[1].take(2)
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
            onSnackbarDismissed = {}
        )
    }
}
