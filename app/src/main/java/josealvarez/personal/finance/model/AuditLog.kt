package josealvarez.personal.finance.model

import com.google.firebase.Timestamp

data class AuditLog(
    val id: String = "",
    val action: String = "", // e.g., "ADD_EXPENSE", "DELETE_EXPENSE", "UPDATE_BUDGET"
    val details: String = "",
    val amount: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val metadata: Map<String, Any> = emptyMap()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "action" to action,
        "details" to details,
        "amount" to amount,
        "timestamp" to timestamp,
        "metadata" to metadata
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): AuditLog = AuditLog(
            id = id,
            action = map["action"] as? String ?: "",
            details = map["details"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
            metadata = map["metadata"] as? Map<String, Any> ?: emptyMap()
        )
    }
}
