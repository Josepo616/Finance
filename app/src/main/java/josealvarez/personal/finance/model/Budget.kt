package josealvarez.personal.finance.model

data class Budget(
    val availableFunds: Double = 0.0,
    val dailyLimit: Double = 0.0,
    val weeklyLimit: Double = 0.0,
    val monthlyLimit: Double = 0.0,
    val originalWeeklyLimit: Double = 0.0,
    val currentWeeklyLimit: Double = 0.0,
    val originalDailyLimit: Double = 0.0,
    val currentDailyLimit: Double = 0.0,
    val originalMonthlyLimit: Double = 0.0,
    val currentMonthlyLimit: Double = 0.0,
    val lastResetDate: String = "",
    val lastDailyResetDate: String = "",
    val lastMonthlyResetDate: String = "",
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
        "originalDailyLimit" to originalDailyLimit,
        "currentDailyLimit" to currentDailyLimit,
        "originalMonthlyLimit" to originalMonthlyLimit,
        "currentMonthlyLimit" to currentMonthlyLimit,
        "lastResetDate" to lastResetDate,
        "lastDailyResetDate" to lastDailyResetDate,
        "lastMonthlyResetDate" to lastMonthlyResetDate,
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
            originalDailyLimit = (map["originalDailyLimit"] as? Number)?.toDouble() ?: (map["dailyLimit"] as? Number)?.toDouble() ?: 0.0,
            currentDailyLimit = (map["currentDailyLimit"] as? Number)?.toDouble() ?: (map["dailyLimit"] as? Number)?.toDouble() ?: 0.0,
            originalMonthlyLimit = (map["originalMonthlyLimit"] as? Number)?.toDouble() ?: (map["monthlyLimit"] as? Number)?.toDouble() ?: 0.0,
            currentMonthlyLimit = (map["currentMonthlyLimit"] as? Number)?.toDouble() ?: (map["monthlyLimit"] as? Number)?.toDouble() ?: 0.0,
            lastResetDate = map["lastResetDate"] as? String ?: "",
            lastDailyResetDate = map["lastDailyResetDate"] as? String ?: "",
            lastMonthlyResetDate = map["lastMonthlyResetDate"] as? String ?: "",
            currentWeekStartDate = map["currentWeekStartDate"] as? String ?: "",
            currentWeekEndDate = map["currentWeekEndDate"] as? String ?: ""
        )
    }
}
