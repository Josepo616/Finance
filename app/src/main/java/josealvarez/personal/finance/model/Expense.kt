package josealvarez.personal.finance.model

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: Category = Category.OTHER,
    val description: String = "",
    val date: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "category" to category.name,
        "description" to description,
        "date" to date,
        "createdAt" to createdAt,
        "isDeleted" to isDeleted
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Expense = Expense(
            id = id,
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            category = try {
                Category.valueOf(map["category"] as? String ?: "OTHER")
            } catch (_: IllegalArgumentException) {
                Category.OTHER
            },
            description = map["description"] as? String ?: "",
            date = map["date"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
            isDeleted = map["isDeleted"] as? Boolean ?: false
        )
    }
}
