package josealvarez.personal.finance.ui.category

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

class CategoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: CategoryViewModel = viewModel(
                        factory = CategoryViewModelFactory(uid)
                    )
                    val uiState by viewModel.uiState.collectAsState()

                    if (uiState.showEditScreen) {
                        CategoryEditScreen(
                            uiState = uiState,
                            onSaveCategory = { category ->
                                if (uiState.editingCategory == null) {
                                    viewModel.addCategory(category)
                                } else {
                                    viewModel.updateCategory(category)
                                }
                            },
                            onBack = { viewModel.hideEditForm() },
                            onSnackbarDismissed = { viewModel.clearSnackbar() }
                        )
                    } else {
                        CategoryListScreen(
                            uiState = uiState,
                            onAddClick = { viewModel.showEditForm() },
                            onCategoryClick = { category -> viewModel.showEditForm(category) },
                            onDeleteCategory = { categoryId -> viewModel.deleteCategory(categoryId) },
                            onBack = { finish() },
                            onSnackbarDismissed = { viewModel.clearSnackbar() }
                        )
                    }
                }
            }
        }
    }
}
