package josealvarez.personal.finance.model

import com.google.firebase.Timestamp

enum class BudgetAllocation {
    DAILY, WEEKLY, MONTHLY, NONE
}

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "",
    val categoryName: String = "Other",
    val description: String = "",
    val date: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false,
    val budgetAllocation: BudgetAllocation = BudgetAllocation.WEEKLY
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "description" to description,
        "date" to date,
        "createdAt" to createdAt,
        "isDeleted" to isDeleted,
        "budgetAllocation" to budgetAllocation.name
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Expense {
            val allocationStr = map["budgetAllocation"] as? String
            val budgetAllocation = if (allocationStr != null) {
                try {
                    BudgetAllocation.valueOf(allocationStr)
                } catch (e: Exception) {
                    BudgetAllocation.WEEKLY
                }
            } else {
                BudgetAllocation.WEEKLY
            }

            return Expense(
                id = id,
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                categoryId = map["categoryId"] as? String ?: "",
                categoryName = map["categoryName"] as? String ?: map["category"] as? String ?: "Other",
                description = map["description"] as? String ?: "",
                date = map["date"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                isDeleted = map["isDeleted"] as? Boolean ?: false,
                budgetAllocation = budgetAllocation
            )
        }
    }
}
