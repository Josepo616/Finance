package josealvarez.personal.finance.model

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val month: Int,
    val year: Int
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyTrend(
    val month: Int,
    val year: Int,
    val label: String, // e.g. "Jan"
    val totalExpenses: Double
)

data class BudgetComparison(
    val label: String, // "Daily", "Weekly", "Monthly"
    val limit: Double,
    val actual: Double,
    val percentage: Float
)
