package josealvarez.personal.finance.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.AuditRepository
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.model.AuditLog
import josealvarez.personal.finance.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val showAddScreen: Boolean = false
)

class ExpenseViewModel(
    private val uid: String,
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val budgetRepository: BudgetRepository = BudgetRepository(),
    private val auditRepository: AuditRepository = AuditRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val expenses = expenseRepository.getMonthlyExpenses(uid, year, month)
                _uiState.value = _uiState.value.copy(expenses = expenses, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load expenses"
                )
            }
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                expenseRepository.addExpense(uid, expense)

                val budget = budgetRepository.getBudget(uid)
                val updatedBudget = budget.copy(
                    availableFunds = budget.availableFunds - expense.amount,
                    currentWeeklyLimit = budget.currentWeeklyLimit - expense.amount
                )
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "ADD_EXPENSE",
                        details = "Added expense: ${expense.description} (${expense.category})",
                        amount = expense.amount,
                        metadata = mapOf("category" to expense.category.name, "date" to expense.date)
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    showAddScreen = false
                )
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to add expense"
                )
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                expenseRepository.softDeleteExpense(uid, expense.id)

                val budget = budgetRepository.getBudget(uid)
                val updatedBudget = budget.copy(
                    availableFunds = budget.availableFunds + expense.amount,
                    currentWeeklyLimit = budget.currentWeeklyLimit + expense.amount
                )
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "DELETE_EXPENSE",
                        details = "Deleted expense: ${expense.description} (Restored to budget)",
                        amount = expense.amount,
                        metadata = mapOf("expenseId" to expense.id)
                    )
                )

                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete expense"
                )
            }
        }
    }

    fun showAddForm() {
        _uiState.value = _uiState.value.copy(showAddScreen = true)
    }

    fun hideAddForm() {
        _uiState.value = _uiState.value.copy(showAddScreen = false)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(errorMessage = null, saveSuccess = false)
    }
}

class ExpenseViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
