package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.Budget
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source
import java.text.SimpleDateFormat
import java.util.*

class BudgetRepository {

    private val db = FirestoreConfig.db

    suspend fun getBudget(uid: String): Budget {
        val snapshot = try {
            db.collection("users").document(uid)
                .collection("budget").document("current")
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("budget").document("current")
                .get(Source.CACHE)
                .await()
        }

        return if (snapshot.exists()) {
            Budget.fromMap(snapshot.data ?: emptyMap())
        } else {
            Budget()
        }
    }

    suspend fun getHistoricalDailyLimit(uid: String, dateStr: String): Double? {
        val snapshot = db.collection("users").document(uid)
            .collection("budget_history_daily").document(dateStr)
            .get().await()
        return snapshot.getDouble("limit")
    }

    suspend fun getHistoricalWeeklyLimit(uid: String, weekId: String): Double? {
        val snapshot = db.collection("users").document(uid)
            .collection("budget_history_weekly").document(weekId)
            .get().await()
        return snapshot.getDouble("limit")
    }

    suspend fun getHistoricalMonthlyLimit(uid: String, monthId: String): Double? {
        val snapshot = db.collection("users").document(uid)
            .collection("budget_history_monthly").document(monthId)
            .get().await()
        return snapshot.getDouble("limit")
    }

    suspend fun saveBudget(uid: String, budget: Budget) {
        // 1. Save current state
        db.collection("users").document(uid)
            .collection("budget").document("current")
            .set(budget.toMap())
            .await()

        val now = Date()
        val cal = Calendar.getInstance()
        cal.time = now

        // 2. Save Daily History Snapshot
        val dailyId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        db.collection("users").document(uid)
            .collection("budget_history_daily").document(dailyId)
            .set(mapOf("limit" to budget.dailyLimit))

        // 3. Save Weekly History Snapshot
        val weekId = "%04d-W%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
        db.collection("users").document(uid)
            .collection("budget_history_weekly").document(weekId)
            .set(mapOf("limit" to budget.weeklyLimit))

        // 4. Save Monthly History Snapshot
        val monthId = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now)
        db.collection("users").document(uid)
            .collection("budget_history_monthly").document(monthId)
            .set(mapOf("limit" to budget.monthlyLimit))
    }
}
