package josealvarez.personal.finance.ui.expense

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import josealvarez.personal.finance.DashboardActivity
import josealvarez.personal.finance.LoginActivity
import josealvarez.personal.finance.R
import josealvarez.personal.finance.ui.budget.BudgetActivity
import josealvarez.personal.finance.ui.category.CategoryActivity
import josealvarez.personal.finance.ui.components.FinanceAppScaffold
import josealvarez.personal.finance.ui.components.NavigationItem
import josealvarez.personal.finance.ui.income.IncomeActivity

class ExpenseActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            finish()
            return
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            MaterialTheme {
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(user.uid)
                )
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.showAddScreen) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AddExpenseScreen(
                            uiState = uiState,
                            onAddExpense = { expense -> viewModel.addExpense(expense) },
                            onBack = { viewModel.hideAddForm() },
                            onSnackbarDismissed = { viewModel.clearSnackbar() }
                        )
                    }
                } else {
                    FinanceAppScaffold(
                        title = "Expenses",
                        userName = user.displayName ?: "No Name",
                        userEmail = user.email ?: "No Email",
                        onLogoutClick = { signOut() },
                        onNavigate = { item ->
                            when (item) {
                                NavigationItem.Dashboard -> {
                                    val intent = Intent(this, DashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    startActivity(intent)
                                    finish()
                                }
                                NavigationItem.Budget -> {
                                    startActivity(Intent(this, BudgetActivity::class.java))
                                    finish()
                                }
                                NavigationItem.Expenses -> { /* Already here */ }
                                NavigationItem.Income -> {
                                    startActivity(Intent(this, IncomeActivity::class.java))
                                    finish()
                                }
                                NavigationItem.Categories -> {
                                    startActivity(Intent(this, CategoryActivity::class.java))
                                    finish()
                                }
                            }
                        },
                        selectedItem = NavigationItem.Expenses,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { padding ->
                        Surface(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            ExpenseListScreen(
                                uiState = uiState,
                                onAddClick = { viewModel.showAddForm() },
                                onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
                                onNavigateMonth = { delta -> viewModel.navigateMonth(delta) },
                                onBack = { finish() },
                                onSnackbarDismissed = { viewModel.clearSnackbar() },
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
