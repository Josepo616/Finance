package josealvarez.personal.finance.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.AuditRepository
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.model.AuditLog
import josealvarez.personal.finance.model.Budget
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
    private val auditRepository: AuditRepository = AuditRepository()
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
                val resetBudget = checkAndResetWeeklyLimit(budget)
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

    private fun checkAndResetWeeklyLimit(budget: Budget): Budget {
        if (budget.currentWeekStartDate.isEmpty() || budget.currentWeekEndDate.isEmpty()) return budget

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        
        return if (todayStr > budget.currentWeekEndDate) {
            // Auto-advance to next week if current one ended
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(budget.currentWeekEndDate)!!
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val newStart = sdf.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val newEnd = sdf.format(calendar.time)
            
            budget.copy(
                currentWeekStartDate = newStart,
                currentWeekEndDate = newEnd,
                currentWeeklyLimit = budget.originalWeeklyLimit,
                lastResetDate = todayStr
            )
        } else {
            budget
        }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                val oldBudget = repository.getBudget(uid)

                val updatedBudget = if (budget.currentWeekStartDate.isNotEmpty() && budget.currentWeekEndDate.isNotEmpty()) {
                    val expenses = expenseRepository.getExpensesInRange(
                        uid, 
                        budget.currentWeekStartDate, 
                        budget.currentWeekEndDate
                    )
                    val totalSpent = expenses.sumOf { it.amount }
                    budget.copy(currentWeeklyLimit = budget.originalWeeklyLimit - totalSpent)
                } else {
                    budget
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
