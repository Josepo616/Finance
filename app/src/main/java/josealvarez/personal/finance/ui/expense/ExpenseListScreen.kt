package josealvarez.personal.finance.ui.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import josealvarez.personal.finance.model.Category
import josealvarez.personal.finance.model.Expense
import josealvarez.personal.finance.ui.common.MonthSelector
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    uiState: ExpenseUiState,
    onAddClick: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onNavigateDate: (Int) -> Unit,
    onViewModeChange: (ExpenseViewMode) -> Unit,
    onDateRangeChange: (String, String) -> Unit,
    onToggleExcludeExempt: () -> Unit,
    onBack: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Expense added successfully")
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
        Column(modifier = Modifier.fillMaxSize()) {
            
            // View Mode Toggle
            TabRow(
                selectedTabIndex = when(uiState.viewMode) {
                    ExpenseViewMode.MONTHLY -> 0
                    ExpenseViewMode.DAILY -> 1
                    ExpenseViewMode.RANGE -> 2
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.viewMode == ExpenseViewMode.MONTHLY,
                    onClick = { onViewModeChange(ExpenseViewMode.MONTHLY) },
                    text = { Text("Monthly", fontSize = 12.sp) }
                )
                Tab(
                    selected = uiState.viewMode == ExpenseViewMode.DAILY,
                    onClick = { onViewModeChange(ExpenseViewMode.DAILY) },
                    text = { Text("Daily", fontSize = 12.sp) }
                )
                Tab(
                    selected = uiState.viewMode == ExpenseViewMode.RANGE,
                    onClick = { onViewModeChange(ExpenseViewMode.RANGE) },
                    text = { Text("Range", fontSize = 12.sp) }
                )
            }

            when (uiState.viewMode) {
                ExpenseViewMode.MONTHLY -> {
                    MonthSelector(
                        month = uiState.selectedMonth,
                        year = uiState.selectedYear,
                        onPrevious = { onNavigateDate(-1) },
                        onNext = { onNavigateDate(1) },
                        isNextEnabled = !uiState.isCurrentMonth
                    )
                }
                ExpenseViewMode.DAILY -> {
                    DateSelector(
                        day = uiState.selectedDay,
                        month = uiState.selectedMonth,
                        year = uiState.selectedYear,
                        onPrevious = { onNavigateDate(-1) },
                        onNext = { onNavigateDate(1) },
                        isNextEnabled = !uiState.isCurrentDay
                    )
                }
                ExpenseViewMode.RANGE -> {
                    RangeSelector(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        onRangeChange = onDateRangeChange
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.expenses.isEmpty()) {
                    Text(
                        text = when(uiState.viewMode) {
                            ExpenseViewMode.MONTHLY -> "No expenses this month"
                            ExpenseViewMode.DAILY -> "No expenses on this date"
                            ExpenseViewMode.RANGE -> "No expenses in this range"
                        },
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val total = if (uiState.viewMode == ExpenseViewMode.RANGE && uiState.excludeExemptFromTotal) {
                                uiState.expenses.filter { expense ->
                                    val isExcluded = uiState.categories.find { it.id == expense.categoryId }?.excludeFromWeeklyLimit ?: false
                                    !isExcluded
                                }.sumOf { it.amount }
                            } else {
                                uiState.expenses.sumOf { it.amount }
                            }

                            val totalLabel = when(uiState.viewMode) {
                                ExpenseViewMode.MONTHLY -> "Monthly Total"
                                ExpenseViewMode.DAILY -> "Daily Total"
                                ExpenseViewMode.RANGE -> if (uiState.excludeExemptFromTotal) "Range Total (Excl. Exempt)" else "Range Total"
                            }
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = totalLabel,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "$%.2f".format(total),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (uiState.viewMode == ExpenseViewMode.RANGE) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onToggleExcludeExempt() }
                                        ) {
                                            Text("Hide Exempt", style = MaterialTheme.typography.labelSmall)
                                            Checkbox(
                                                checked = uiState.excludeExemptFromTotal,
                                                onCheckedChange = { onToggleExcludeExempt() },
                                                scale = 0.8f
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(uiState.expenses) { expense ->
                            val isExcluded = uiState.categories.find { it.id == expense.categoryId }?.excludeFromWeeklyLimit ?: false
                            ExpenseItem(
                                expense = expense,
                                isExcluded = isExcluded,
                                onDelete = { onDeleteExpense(expense) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }
}

// Helper for smaller checkbox
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
) {
    androidx.compose.material3.Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSelector(
    startDate: String,
    endDate: String,
    onRangeChange: (String, String) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateFormat.parse(startDate)?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onRangeChange(dateFormat.format(Date(millis)), endDate)
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateFormat.parse(endDate)?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onRangeChange(startDate, dateFormat.format(Date(millis)))
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).clickable { showStartPicker = true }) {
            Text("From", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(startDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
        Column(modifier = Modifier.weight(1f).clickable { showEndPicker = true }, horizontalAlignment = Alignment.End) {
            Text("To", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(endDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DateSelector(
    day: Int,
    month: Int,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatSymbols().months[month - 1]
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Day"
            )
        }
        
        Text(
            text = "$monthName $day, $year",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        IconButton(
            onClick = onNext,
            enabled = isNextEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Day"
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    isExcluded: Boolean,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.categoryName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (isExcluded) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "Exempt",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    Text(
                        text = "$%.2f".format(expense.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isExcluded) MaterialTheme.colorScheme.outline else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = expense.description.ifBlank { "No description" },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = expense.date,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListScreenPreview() {
    MaterialTheme {
        ExpenseListScreen(
            uiState = ExpenseUiState(
                expenses = listOf(
                    Expense(
                        id = "1",
                        amount = 25.50,
                        categoryName = "Food",
                        description = "Lunch",
                        date = "2026-03-01"
                    )
                )
            ),
            onAddClick = {},
            onDeleteExpense = {},
            onNavigateDate = {},
            onViewModeChange = {},
            onDateRangeChange = { _, _ -> },
            onToggleExcludeExempt = {},
            onBack = {},
            onSnackbarDismissed = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
