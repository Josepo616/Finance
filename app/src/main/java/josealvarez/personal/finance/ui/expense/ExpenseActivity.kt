package josealvarez.personal.finance.ui.expense

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

class ExpenseActivity : ComponentActivity() {

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
                    val viewModel: ExpenseViewModel = viewModel(
                        factory = ExpenseViewModelFactory(uid)
                    )
                    val uiState by viewModel.uiState.collectAsState()

                    if (uiState.showAddScreen) {
                        AddExpenseScreen(
                            uiState = uiState,
                            onAddExpense = { expense -> viewModel.addExpense(expense) },
                            onBack = { viewModel.hideAddForm() },
                            onSnackbarDismissed = { viewModel.clearSnackbar() }
                        )
                    } else {
                        ExpenseListScreen(
                            uiState = uiState,
                            onAddClick = { viewModel.showAddForm() },
                            onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
                            onBack = { finish() },
                            onSnackbarDismissed = { viewModel.clearSnackbar() }
                        )
                    }
                }
            }
        }
    }
}
