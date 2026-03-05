package josealvarez.personal.finance.model

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "",
    val categoryName: String = "Other",
    val description: String = "",
    val date: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "description" to description,
        "date" to date,
        "createdAt" to createdAt,
        "isDeleted" to isDeleted
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Expense = Expense(
            id = id,
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            categoryId = map["categoryId"] as? String ?: "",
            categoryName = map["categoryName"] as? String ?: map["category"] as? String ?: "Other",
            description = map["description"] as? String ?: "",
            date = map["date"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
            isDeleted = map["isDeleted"] as? Boolean ?: false
        )
    }
}
