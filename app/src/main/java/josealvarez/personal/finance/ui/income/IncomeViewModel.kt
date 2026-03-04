package josealvarez.personal.finance.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.AuditRepository
import josealvarez.personal.finance.data.repository.BudgetRepository
import josealvarez.personal.finance.data.repository.IncomeRepository
import josealvarez.personal.finance.model.AuditLog
import josealvarez.personal.finance.model.Income
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class IncomeUiState(
    val incomeList: List<Income> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val showAddScreen: Boolean = false
)

class IncomeViewModel(
    private val uid: String,
    private val incomeRepository: IncomeRepository = IncomeRepository(),
    private val budgetRepository: BudgetRepository = BudgetRepository(),
    private val auditRepository: AuditRepository = AuditRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadIncome()
    }

    fun loadIncome() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val incomeList = incomeRepository.getMonthlyIncome(uid, year, month)
                _uiState.value = _uiState.value.copy(incomeList = incomeList, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load income"
                )
            }
        }
    }

    fun addIncome(income: Income) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                incomeRepository.addIncome(uid, income)

                val budget = budgetRepository.getBudget(uid)
                val updatedBudget = budget.copy(
                    availableFunds = budget.availableFunds + income.amount
                )
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "ADD_INCOME",
                        details = "Added income: ${income.description} (${income.category})",
                        amount = income.amount,
                        metadata = mapOf("category" to income.category.name, "date" to income.date)
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    showAddScreen = false
                )
                loadIncome()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to add income"
                )
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                incomeRepository.softDeleteIncome(uid, income.id)

                val budget = budgetRepository.getBudget(uid)
                val updatedBudget = budget.copy(
                    availableFunds = budget.availableFunds - income.amount
                )
                budgetRepository.saveBudget(uid, updatedBudget)

                auditRepository.logAction(
                    uid, AuditLog(
                        action = "DELETE_INCOME",
                        details = "Deleted income: ${income.description} (Deducted from budget)",
                        amount = income.amount,
                        metadata = mapOf("incomeId" to income.id)
                    )
                )

                loadIncome()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete income"
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

class IncomeViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeViewModel::class.java)) {
            return IncomeViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
