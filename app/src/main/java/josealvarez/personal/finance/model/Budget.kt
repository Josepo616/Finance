package josealvarez.personal.finance.model

data class Budget(
    val availableFunds: Double = 0.0,
    val dailyLimit: Double = 0.0,
    val weeklyLimit: Double = 0.0,
    val monthlyLimit: Double = 0.0,
    val originalWeeklyLimit: Double = 0.0,
    val currentWeeklyLimit: Double = 0.0,
    val lastResetDate: String = "",
    val currentWeekStartDate: String = "",
    val currentWeekEndDate: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "availableFunds" to availableFunds,
        "dailyLimit" to dailyLimit,
        "weeklyLimit" to weeklyLimit,
        "monthlyLimit" to monthlyLimit,
        "originalWeeklyLimit" to originalWeeklyLimit,
        "currentWeeklyLimit" to currentWeeklyLimit,
        "lastResetDate" to lastResetDate,
        "currentWeekStartDate" to currentWeekStartDate,
        "currentWeekEndDate" to currentWeekEndDate
    )

    companion object {
        fun fromMap(map: Map<String, Any>): Budget = Budget(
            availableFunds = (map["availableFunds"] as? Number)?.toDouble() ?: 0.0,
            dailyLimit = (map["dailyLimit"] as? Number)?.toDouble() ?: 0.0,
            weeklyLimit = (map["weeklyLimit"] as? Number)?.toDouble() ?: 0.0,
            monthlyLimit = (map["monthlyLimit"] as? Number)?.toDouble() ?: 0.0,
            originalWeeklyLimit = (map["originalWeeklyLimit"] as? Number)?.toDouble() ?: 0.0,
            currentWeeklyLimit = (map["currentWeeklyLimit"] as? Number)?.toDouble() ?: 0.0,
            lastResetDate = map["lastResetDate"] as? String ?: "",
            currentWeekStartDate = map["currentWeekStartDate"] as? String ?: "",
            currentWeekEndDate = map["currentWeekEndDate"] as? String ?: ""
        )
    }
}
