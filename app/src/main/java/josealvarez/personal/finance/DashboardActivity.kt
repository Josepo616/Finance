package josealvarez.personal.finance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import josealvarez.personal.finance.model.Budget
import josealvarez.personal.finance.ui.budget.BudgetActivity
import josealvarez.personal.finance.ui.category.CategoryActivity
import josealvarez.personal.finance.ui.components.FinanceAppScaffold
import josealvarez.personal.finance.ui.components.NavigationItem
import josealvarez.personal.finance.ui.expense.ExpenseActivity
import josealvarez.personal.finance.ui.income.IncomeActivity

class DashboardActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            navigateToLogin()
            return
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MaterialTheme {
                FinanceAppScaffold(
                    title = "Dashboard",
                    userName = user.displayName ?: "No Name",
                    userEmail = user.email ?: "No Email",
                    onLogoutClick = { signOut() },
                    onNavigate = { item ->
                        when (item) {
                            NavigationItem.Dashboard -> { /* Already here */ }
                            NavigationItem.Budget -> startActivity(Intent(this, BudgetActivity::class.java))
                            NavigationItem.Expenses -> startActivity(Intent(this, ExpenseActivity::class.java))
                            NavigationItem.Income -> startActivity(Intent(this, IncomeActivity::class.java))
                            NavigationItem.Categories -> startActivity(Intent(this, CategoryActivity::class.java))
                        }
                    },
                    selectedItem = NavigationItem.Dashboard
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DashboardScreen(
                            uiState = uiState,
                            onBudgetClick = {
                                startActivity(Intent(this@DashboardActivity, BudgetActivity::class.java))
                            },
                            onExpensesClick = {
                                startActivity(Intent(this@DashboardActivity, ExpenseActivity::class.java))
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to dashboard
        if (auth.currentUser != null) {
            viewModel.loadDashboardData()
        }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onBudgetClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Financial Status",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        WeeklyRemainingCard(budget = uiState.budget)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Add Expense",
                icon = Icons.Default.Add,
                onClick = onExpensesClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Quick View",
                icon = Icons.Default.Visibility,
                onClick = onBudgetClick,
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun WeeklyRemainingCard(budget: Budget) {
    val remaining = budget.currentWeeklyLimit
    val original = budget.originalWeeklyLimit
    val isExceeded = remaining < 0
    
    val backgroundColor = if (isExceeded) Color.DarkGray else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isExceeded) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    
    val displayAmount = if (isExceeded) Math.abs(remaining) else remaining
    val titleText = if (isExceeded) "Limit Exceeded" else "Weekly Remaining"
    val subtitleText = if (isExceeded) {
        "You exceeded by $${"%.2f".format(displayAmount)} from your $${"%.2f".format(original)} limit"
    } else {
        "of $${"%.2f".format(original)} limit"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExceeded) "$${"%.2f".format(displayAmount)}" else "$${"%.2f".format(displayAmount)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitleText,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer { alpha = 0.8f }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DashboardScreen(
                uiState = DashboardUiState(
                    budget = Budget(
                        originalWeeklyLimit = 500.0,
                        currentWeeklyLimit = 400.0
                    )
                ),
                onBudgetClick = {},
                onExpensesClick = {}
            )
        }
    }
}
