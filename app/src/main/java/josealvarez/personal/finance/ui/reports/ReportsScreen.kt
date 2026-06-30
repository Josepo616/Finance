package josealvarez.personal.finance.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import josealvarez.personal.finance.ui.common.MonthSelector
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    
    val isNextEnabled = uiState.selectedYear < currentYear || 
            (uiState.selectedYear == currentYear && uiState.selectedMonth < currentMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    MonthSelector(
                        month = uiState.selectedMonth,
                        year = uiState.selectedYear,
                        onPrevious = { viewModel.navigateMonth(-1) },
                        onNext = { viewModel.navigateMonth(1) },
                        isNextEnabled = isNextEnabled
                    )
                }

                if (uiState.isLoading && uiState.summary == null) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    uiState.summary?.let { summary ->
                        item {
                            SummaryCard(
                                income = summary.totalIncome,
                                expenses = summary.totalExpenses,
                                balance = summary.netBalance
                            )
                        }
                        
                        item {
                            CategoryBreakdownChart(spending = uiState.categoryBreakdown)
                        }
                        
                        item {
                            MonthlyTrendChart(
                                trend = uiState.monthlyTrend,
                                selectedMonth = uiState.selectedMonth,
                                selectedYear = uiState.selectedYear
                            )
                        }
                        
                        item {
                            BudgetComparisonCard(comparisons = uiState.budgetComparison)
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.summary != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearSnackbar() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    income: Double,
    expenses: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    label = "Income",
                    amount = income,
                    color = Color(0xFF34A853), // Green
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Expenses",
                    amount = expenses,
                    color = Color(0xFFEA4335), // Red
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Balance",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$%.2f".format(balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Color(0xFF34A853) else Color(0xFFEA4335)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$%.2f".format(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
