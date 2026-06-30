package josealvarez.personal.finance.ui.reports

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
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
import josealvarez.personal.finance.ui.expense.ExpenseActivity
import josealvarez.personal.finance.ui.income.IncomeActivity

class ReportsActivity : ComponentActivity() {

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
            MaterialTheme {
                val viewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModelFactory(user.uid)
                )

                FinanceAppScaffold(
                    title = "Reports",
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
                            NavigationItem.Expenses -> {
                                startActivity(Intent(this, ExpenseActivity::class.java))
                                finish()
                            }
                            NavigationItem.Income -> {
                                startActivity(Intent(this, IncomeActivity::class.java))
                                finish()
                            }
                            NavigationItem.Categories -> {
                                startActivity(Intent(this, CategoryActivity::class.java))
                                finish()
                            }
                            NavigationItem.Reports -> { /* Already here */ }
                        }
                    },
                    selectedItem = NavigationItem.Reports
                ) { padding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ReportsScreen(
                            viewModel = viewModel,
                            onBackClick = { finish() }
                        )
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
