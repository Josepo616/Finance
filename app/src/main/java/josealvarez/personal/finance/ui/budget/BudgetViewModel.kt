package josealvarez.personal.finance.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.AuditRepository
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.CategoryRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.model.AuditLog
import josealvarez.personal.finance.model.Budget
import josealvarez.personal.finance.model.BudgetAllocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BudgetUiState(
    val budget: Budget = Budget(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class BudgetViewModel(
    private val uid: String,
    private val repository: BudgetRepository = BudgetRepository(),
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val auditRepository: AuditRepository = AuditRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                var budget = repository.getBudget(uid)
                val resetBudget = checkAndResetLimits(budget)
                if (resetBudget != budget) {
                    repository.saveBudget(uid, resetBudget)
                    budget = resetBudget
                }
                _uiState.value = _uiState.value.copy(budget = budget, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load budget"
                )
            }
        }
    }

    private fun checkAndResetLimits(budget: Budget): Budget {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val currentMonthStr = todayStr.substring(0, 7)

        var updatedBudget = budget

        // 1. Daily Reset Check
        if (updatedBudget.lastDailyResetDate.isEmpty() || todayStr > updatedBudget.lastDailyResetDate) {
            updatedBudget = updatedBudget.copy(
                currentDailyLimit = updatedBudget.originalDailyLimit,
                lastDailyResetDate = todayStr
            )
        }

        // 2. Monthly Reset Check
        if (updatedBudget.lastMonthlyResetDate.isEmpty() || currentMonthStr > updatedBudget.lastMonthlyResetDate) {
            updatedBudget = updatedBudget.copy(
                currentMonthlyLimit = updatedBudget.originalMonthlyLimit,
                lastMonthlyResetDate = currentMonthStr
            )
        }

        // 3. Weekly Reset Check
        if (updatedBudget.currentWeekStartDate.isNotEmpty() && updatedBudget.currentWeekEndDate.isNotEmpty()) {
            if (todayStr > updatedBudget.currentWeekEndDate) {
                val calendar = Calendar.getInstance()
                calendar.time = sdf.parse(updatedBudget.currentWeekEndDate)!!
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val newStart = sdf.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val newEnd = sdf.format(calendar.time)

                updatedBudget = updatedBudget.copy(
                    currentWeekStartDate = newStart,
                    currentWeekEndDate = newEnd,
                    currentWeeklyLimit = updatedBudget.originalWeeklyLimit,
                    lastResetDate = todayStr
                )
            }
        }

        return updatedBudget
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                val oldBudget = repository.getBudget(uid)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                val cal = Calendar.getInstance()
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH) + 1
                val currentMonthStr = todayStr.substring(0, 7)

                val dailyExpenses = expenseRepository.getExpensesInRange(uid, todayStr, todayStr)
                val monthlyExpenses = expenseRepository.getMonthlyExpenses(uid, currentYear, currentMonth)

                val dailySpent = dailyExpenses.filter { it.budgetAllocation == BudgetAllocation.DAILY }.sumOf { it.amount }
                val monthlySpent = monthlyExpenses.filter { 
                    it.budgetAllocation == BudgetAllocation.MONTHLY || 
                    it.budgetAllocation == BudgetAllocation.WEEKLY || 
                    it.budgetAllocation == BudgetAllocation.DAILY 
                }.sumOf { it.amount }

                var updatedBudget = budget.copy(
                    originalDailyLimit = budget.dailyLimit,
                    currentDailyLimit = budget.dailyLimit - dailySpent,
                    lastDailyResetDate = todayStr,
                    originalMonthlyLimit = budget.monthlyLimit,
                    currentMonthlyLimit = budget.monthlyLimit - monthlySpent,
                    lastMonthlyResetDate = currentMonthStr
                )

                if (updatedBudget.currentWeekStartDate.isNotEmpty() && updatedBudget.currentWeekEndDate.isNotEmpty()) {
                    val expenses = expenseRepository.getExpensesInRange(
                        uid, 
                        updatedBudget.currentWeekStartDate, 
                        updatedBudget.currentWeekEndDate
                    )
                    
                    val categories = categoryRepository.getCategories(uid)
                    val exemptCategoryIds = categories.filter { it.excludeFromWeeklyLimit }.map { it.id }.toSet()
                    
                    val totalSpent = expenses.filter { 
                        (it.budgetAllocation == BudgetAllocation.WEEKLY || it.budgetAllocation == BudgetAllocation.DAILY) && 
                        it.categoryId !in exemptCategoryIds 
                    }.sumOf { it.amount }
                    updatedBudget = updatedBudget.copy(currentWeeklyLimit = updatedBudget.originalWeeklyLimit - totalSpent)
                }

                repository.saveBudget(uid, updatedBudget)

                // Log significant changes
                val logDetails = mutableListOf<String>()
                if (oldBudget.availableFunds != updatedBudget.availableFunds) {
                    logDetails.add("Funds: ${oldBudget.availableFunds} -> ${updatedBudget.availableFunds}")
                }
                if (oldBudget.originalWeeklyLimit != updatedBudget.originalWeeklyLimit) {
                    logDetails.add("Weekly Limit: ${oldBudget.originalWeeklyLimit} -> ${updatedBudget.originalWeeklyLimit}")
                }
                if (oldBudget.currentWeekStartDate != updatedBudget.currentWeekStartDate) {
                    logDetails.add("Period: ${updatedBudget.currentWeekStartDate} to ${updatedBudget.currentWeekEndDate}")
                }

                if (logDetails.isNotEmpty()) {
                    auditRepository.logAction(
                        uid, AuditLog(
                            action = "UPDATE_BUDGET",
                            details = "Budget updated: ${logDetails.joinToString(", ")}",
                            metadata = mapOf("old" to oldBudget.toMap(), "new" to updatedBudget.toMap())
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    budget = updatedBudget,
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save budget"
                )
            }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(errorMessage = null, saveSuccess = false)
    }
}

class BudgetViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
