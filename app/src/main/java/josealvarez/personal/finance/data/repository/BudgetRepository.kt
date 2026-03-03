package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.Budget
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source

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

    suspend fun saveBudget(uid: String, budget: Budget) {
        db.collection("users").document(uid)
            .collection("budget").document("current")
            .set(budget.toMap())
    }
}
