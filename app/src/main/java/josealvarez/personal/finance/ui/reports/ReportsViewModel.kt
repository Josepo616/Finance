package josealvarez.personal.finance.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.CategoryRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.data.repository.IncomeRepository
import josealvarez.personal.finance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReportsUiState(
    val summary: MonthlySummary? = null,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val monthlyTrend: List<MonthlyTrend> = emptyList(),
    val budgetComparison: List<BudgetComparison> = emptyList(),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReportsViewModel(
    private val uid: String,
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val incomeRepository: IncomeRepository = IncomeRepository(),
    private val budgetRepository: BudgetRepository = BudgetRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        val now = Calendar.getInstance()
        loadReport(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
    }

    fun loadReport(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedYear = year,
                selectedMonth = month
            )
            try {
                // 1. Monthly Summary
                val expenses = expenseRepository.getMonthlyExpenses(uid, year, month)
                val income = incomeRepository.getMonthlyIncome(uid, year, month)

                val totalExpenses = expenses.sumOf { it.amount }
                val totalIncome = income.sumOf { it.amount }
                val summary = MonthlySummary(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netBalance = totalIncome - totalExpenses,
                    month = month,
                    year = year
                )

                // 2. Category Breakdown
                val categoryGroups = expenses.groupBy { it.categoryName }
                val categoryBreakdown = categoryGroups.map { (name, list) ->
                    val amount = list.sumOf { it.amount }
                    CategorySpending(
                        category = name,
                        amount = amount,
                        percentage = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f
                    )
                }.sortedByDescending { it.amount }

                // 3. Monthly Trend (Last 6 Months)
                val monthlyTrend = calculateMonthlyTrend(year, month)

                // 4. Budget Comparison
                val budgetComparison = calculateBudgetComparison(year, month, expenses)

                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    categoryBreakdown = categoryBreakdown,
                    monthlyTrend = monthlyTrend,
                    budgetComparison = budgetComparison,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load reports"
                )
            }
        }
    }

    private suspend fun calculateMonthlyTrend(currentYear: Int, currentMonth: Int): List<MonthlyTrend> {
        val trend = mutableListOf<MonthlyTrend>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Fetch last 6 months including current
        for (i in 0 until 6) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val label = monthFormat.format(calendar.time)
            
            val monthlyExpenses = expenseRepository.getMonthlyExpenses(uid, year, month)
            val total = monthlyExpenses.sumOf { it.amount }
            
            trend.add(0, MonthlyTrend(month, year, label, total))
            calendar.add(Calendar.MONTH, -1)
        }
        return trend
    }

    private suspend fun calculateBudgetComparison(year: Int, month: Int, monthlyExpenses: List<Expense>): List<BudgetComparison> {
        val currentBudget = budgetRepository.getBudget(uid)
        val categories = categoryRepository.getCategories(uid)
        val exemptCategoryIds = categories.filter { it.excludeFromWeeklyLimit }.map { it.id }.toSet()
        
        val comparisons = mutableListOf<BudgetComparison>()
        val cal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        // 1. Daily
        val dailyLimit = budgetRepository.getHistoricalDailyLimit(uid, todayStr) ?: currentBudget.dailyLimit
        val dailyActual = monthlyExpenses
            .filter { it.date == todayStr && it.budgetAllocation == BudgetAllocation.DAILY }
            .sumOf { it.amount }
        comparisons.add(BudgetComparison(
            label = "Daily",
            limit = dailyLimit,
            actual = dailyActual,
            percentage = if (dailyLimit > 0) (dailyActual / dailyLimit).toFloat() else 0f
        ))

        // 2. Weekly
        val weekRange = getWeekRange()
        val weekId = "%04d-W%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
        val weeklyLimit = budgetRepository.getHistoricalWeeklyLimit(uid, weekId) ?: currentBudget.weeklyLimit
        val weeklyExpenses = expenseRepository.getExpensesInRange(uid, weekRange.first, weekRange.second)
        val weeklyActual = weeklyExpenses
            .filter { it.budgetAllocation == BudgetAllocation.WEEKLY && it.categoryId !in exemptCategoryIds }
            .sumOf { it.amount }
        comparisons.add(BudgetComparison(
            label = "Weekly",
            limit = weeklyLimit,
            actual = weeklyActual,
            percentage = if (weeklyLimit > 0) (weeklyActual / weeklyLimit).toFloat() else 0f
        ))

        // 3. Monthly
        val monthId = "%04d-%02d".format(year, month)
        val monthlyLimit = budgetRepository.getHistoricalMonthlyLimit(uid, monthId) ?: currentBudget.monthlyLimit
        val monthlyActual = monthlyExpenses
            .filter { it.budgetAllocation == BudgetAllocation.MONTHLY }
            .sumOf { it.amount }
        comparisons.add(BudgetComparison(
            label = "Monthly",
            limit = monthlyLimit,
            actual = monthlyActual,
            percentage = if (monthlyLimit > 0) (monthlyActual / monthlyLimit).toFloat() else 0f
        ))

        return comparisons
    }

    private fun getWeekRange(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        // Set to Monday of current week
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, -6)
        } else {
            cal.add(Calendar.DATE, (Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK)))
        }
        val start = sdf.format(cal.time)

        // Set to Sunday
        cal.add(Calendar.DATE, 6)
        val end = sdf.format(cal.time)

        return Pair(start, end)
    }

    fun navigateMonth(delta: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _uiState.value.selectedYear)
            set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
            add(Calendar.MONTH, delta)
        }
        loadReport(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class ReportsViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
