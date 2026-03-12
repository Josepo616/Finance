package josealvarez.personal.finance.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.AuditRepository
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.CategoryRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.model.AuditLog
import josealvarez.personal.finance.model.Category
import josealvarez.personal.finance.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val showAddScreen: Boolean = false,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isCurrentMonth: Boolean = true
)

class ExpenseViewModel(
    private val uid: String,
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val budgetRepository: BudgetRepository = BudgetRepository(),
    private val auditRepository: AuditRepository = AuditRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
        loadCategories()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            val year = _uiState.value.selectedYear
            val month = _uiState.value.selectedMonth
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
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

    fun navigateMonth(delta: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _uiState.value.selectedYear)
            set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
            add(Calendar.MONTH, delta)
        }

        val newYear = calendar.get(Calendar.YEAR)
        val newMonth = calendar.get(Calendar.MONTH) + 1

        val now = Calendar.getInstance()
        val isCurrentMonth = newYear == now.get(Calendar.YEAR) && newMonth == (now.get(Calendar.MONTH) + 1)

        _uiState.value = _uiState.value.copy(
            selectedYear = newYear,
            selectedMonth = newMonth,
            isCurrentMonth = isCurrentMonth
        )
        loadExpenses()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategories(uid)
                _uiState.value = _uiState.value.copy(categories = categories)
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                expenseRepository.addExpense(uid, expense)

                val budget = budgetRepository.getBudget(uid)
                
                // Fetch category to check exclusion rule
                val category = if (expense.categoryId.isNotBlank()) {
                    categoryRepository.getCategoryById(uid, expense.categoryId)
                } else {
                    null
                }

                val exclude = category?.excludeFromWeeklyLimit ?: false
                
                val updatedBudget = if (exclude) {
                    budget.copy(availableFunds = budget.availableFunds - expense.amount)
                } else {
                    budget.copy(
                        availableFunds = budget.availableFunds - expense.amount,
                        currentWeeklyLimit = budget.currentWeeklyLimit - expense.amount
                    )
                }
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "ADD_EXPENSE",
                        details = "Added expense: ${expense.description} (${expense.categoryName})",
                        amount = expense.amount,
                        metadata = mapOf(
                            "categoryId" to expense.categoryId,
                            "categoryName" to expense.categoryName,
                            "date" to expense.date,
                            "excluded" to exclude
                        )
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
                
                // Fetch category to check exclusion rule
                val category = if (expense.categoryId.isNotBlank()) {
                    categoryRepository.getCategoryById(uid, expense.categoryId)
                } else {
                    null
                }
                
                val exclude = category?.excludeFromWeeklyLimit ?: false

                val updatedBudget = if (exclude) {
                    budget.copy(availableFunds = budget.availableFunds + expense.amount)
                } else {
                    budget.copy(
                        availableFunds = budget.availableFunds + expense.amount,
                        currentWeeklyLimit = budget.currentWeeklyLimit + expense.amount
                    )
                }
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "DELETE_EXPENSE",
                        details = "Deleted expense: ${expense.description} (Restored to budget)",
                        amount = expense.amount,
                        metadata = mapOf("expenseId" to expense.id, "excluded" to exclude)
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
        loadCategories() // Refresh categories when showing form
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
