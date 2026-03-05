package josealvarez.personal.finance.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import josealvarez.personal.finance.data.repository.CategoryRepository
import josealvarez.personal.finance.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val showEditScreen: Boolean = false,
    val editingCategory: Category? = null
)

class CategoryViewModel(
    private val uid: String,
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                categoryRepository.seedDefaultCategories(uid)
                val categories = categoryRepository.getCategories(uid)
                _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load categories"
                )
            }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                categoryRepository.addCategory(uid, category)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    showEditScreen = false
                )
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to add category"
                )
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                categoryRepository.updateCategory(uid, category)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    showEditScreen = false
                )
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to update category"
                )
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                categoryRepository.deleteCategory(uid, categoryId)
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete category"
                )
            }
        }
    }

    fun showEditForm(category: Category? = null) {
        _uiState.value = _uiState.value.copy(showEditScreen = true, editingCategory = category)
    }

    fun hideEditForm() {
        _uiState.value = _uiState.value.copy(showEditScreen = false, editingCategory = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(errorMessage = null, saveSuccess = false)
    }
}

class CategoryViewModelFactory(
    private val uid: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            return CategoryViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
