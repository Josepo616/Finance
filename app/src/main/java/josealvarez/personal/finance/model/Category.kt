package josealvarez.personal.finance.model

import com.google.firebase.Timestamp
import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val excludeFromWeeklyLimit: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "excludeFromWeeklyLimit" to excludeFromWeeklyLimit,
        "isDeleted" to isDeleted,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Category = Category(
            id = id,
            name = map["name"] as? String ?: "",
            excludeFromWeeklyLimit = map["excludeFromWeeklyLimit"] as? Boolean ?: false,
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now()
        )

        val defaultCategories = listOf(
            Category(name = "Food", excludeFromWeeklyLimit = false),
            Category(name = "Transport", excludeFromWeeklyLimit = false),
            Category(name = "Entertainment", excludeFromWeeklyLimit = false),
            Category(name = "Health", excludeFromWeeklyLimit = false),
            Category(name = "Shopping", excludeFromWeeklyLimit = false),
            Category(name = "Bills", excludeFromWeeklyLimit = true),
            Category(name = "Education", excludeFromWeeklyLimit = false),
            Category(name = "Other", excludeFromWeeklyLimit = false)
        )
    }
}
