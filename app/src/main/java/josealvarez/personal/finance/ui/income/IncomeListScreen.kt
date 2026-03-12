package josealvarez.personal.finance.ui.income

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import josealvarez.personal.finance.model.Income
import josealvarez.personal.finance.model.IncomeCategory

import josealvarez.personal.finance.ui.common.MonthSelector

@Composable
fun IncomeListScreen(
    uiState: IncomeUiState,
    onAddClick: () -> Unit,
    onDeleteIncome: (Income) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onBack: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Income added successfully")
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
            MonthSelector(
                month = uiState.selectedMonth,
                year = uiState.selectedYear,
                onPrevious = { onNavigateMonth(-1) },
                onNext = { onNavigateMonth(1) },
                isNextEnabled = !uiState.isCurrentMonth
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.incomeList.isEmpty()) {
                    Text(
                        text = "No income this month",
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
                            val monthlyTotal = uiState.incomeList.sumOf { it.amount }
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Monthly Total",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$%.2f".format(monthlyTotal),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(uiState.incomeList) { income ->
                            IncomeItem(
                                income = income,
                                onDelete = { onDeleteIncome(income) }
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
            Icon(Icons.Default.Add, contentDescription = "Add Income")
        }
    }
}

@Composable
private fun IncomeItem(
    income: Income,
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = income.category.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "$%.2f".format(income.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = income.description.ifBlank { "No description" },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = income.date,
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
private fun IncomeListScreenPreview() {
    MaterialTheme {
        IncomeListScreen(
            uiState = IncomeUiState(
                incomeList = listOf(
                    Income(
                        id = "1",
                        amount = 5000.00,
                        category = IncomeCategory.SALARY,
                        description = "Monthly salary",
                        date = "2026-03-01"
                    ),
                    Income(
                        id = "2",
                        amount = 500.00,
                        category = IncomeCategory.FREELANCE,
                        description = "Web project",
                        date = "2026-03-01"
                    )
                )
            ),
            onAddClick = {},
            onDeleteIncome = {},
            onNavigateMonth = {},
            onBack = {},
            onSnackbarDismissed = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
