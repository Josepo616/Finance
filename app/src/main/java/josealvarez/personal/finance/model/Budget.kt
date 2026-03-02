package josealvarez.personal.finance.model

data class Budget(
    val availableFunds: Double = 0.0,
    val dailyLimit: Double = 0.0,
    val weeklyLimit: Double = 0.0,
    val monthlyLimit: Double = 0.0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "availableFunds" to availableFunds,
        "dailyLimit" to dailyLimit,
        "weeklyLimit" to weeklyLimit,
        "monthlyLimit" to monthlyLimit
    )

    companion object {
        fun fromMap(map: Map<String, Any>): Budget = Budget(
            availableFunds = (map["availableFunds"] as? Number)?.toDouble() ?: 0.0,
            dailyLimit = (map["dailyLimit"] as? Number)?.toDouble() ?: 0.0,
            weeklyLimit = (map["weeklyLimit"] as? Number)?.toDouble() ?: 0.0,
            monthlyLimit = (map["monthlyLimit"] as? Number)?.toDouble() ?: 0.0
        )
    }
}
