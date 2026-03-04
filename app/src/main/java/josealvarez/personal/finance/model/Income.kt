package josealvarez.personal.finance.model

import com.google.firebase.Timestamp

data class Income(
    val id: String = "",
    val amount: Double = 0.0,
    val category: IncomeCategory = IncomeCategory.OTHER,
    val description: String = "",
    val date: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "category" to category.name,
        "description" to description,
        "date" to date,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Income = Income(
            id = id,
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            category = try {
                IncomeCategory.valueOf(map["category"] as? String ?: "OTHER")
            } catch (_: IllegalArgumentException) {
                IncomeCategory.OTHER
            },
            description = map["description"] as? String ?: "",
            date = map["date"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }
}
