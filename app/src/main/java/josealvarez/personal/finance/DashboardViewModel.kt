package josealvarez.personal.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.CategoryRepository
import josealvarez.personal.finance.data.repository.ExpenseRepository
import josealvarez.personal.finance.model.Budget
import josealvarez.personal.finance.model.BudgetAllocation
import josealvarez.personal.finance.model.Category
import josealvarez.personal.finance.model.Expense
import josealvarez.personal.finance.model.FinanceView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val budget: Budget = Budget(),
    val selectedView: FinanceView = FinanceView.CASH_FLOW,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val uid: String,
    private val budgetRepository: BudgetRepository = BudgetRepository(),
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var rawBudget = Budget()
    private var weeklyExpenses: List<Expense> = emptyList()
    private var categories: List<Category> = emptyList()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                rawBudget = budgetRepository.getBudget(uid)
                categories = categoryRepository.getCategories(uid)
                if (rawBudget.currentWeekStartDate.isNotEmpty() && rawBudget.currentWeekEndDate.isNotEmpty()) {
                    weeklyExpenses = expenseRepository.getExpensesInRange(
                        uid,
                        rawBudget.currentWeekStartDate,
                        rawBudget.currentWeekEndDate
                    )
                } else {
                    weeklyExpenses = emptyList()
                }
                updateCalculations()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    fun selectFinanceView(view: FinanceView) {
        _uiState.value = _uiState.value.copy(selectedView = view)
        updateCalculations()
    }

    private fun updateCalculations() {
        val view = _uiState.value.selectedView
        val categoryMap = categories.associateBy { it.id }

        val adjustedBudget = if (view == FinanceView.CASH_FLOW) {
            val adjustment = weeklyExpenses.filter { expense ->
                val category = categoryMap[expense.categoryId]
                val exclude = category?.excludeFromWeeklyLimit ?: false || 
                              (expense.budgetAllocation != BudgetAllocation.WEEKLY && expense.budgetAllocation != BudgetAllocation.DAILY)
                !exclude
            }.sumOf { it.amount - it.personalShare }

            rawBudget.copy(
                currentWeeklyLimit = rawBudget.currentWeeklyLimit - adjustment
            )
        } else {
            rawBudget
        }

        _uiState.value = _uiState.value.copy(
            budget = adjustedBudget,
            isLoading = false
        )
    }
}

class DashboardViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

