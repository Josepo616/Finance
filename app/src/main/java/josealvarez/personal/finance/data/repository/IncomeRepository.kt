package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.Income
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source

class IncomeRepository {

    private val db = FirestoreConfig.db

    suspend fun addIncome(uid: String, income: Income): String {
        val docRef = db.collection("users").document(uid)
            .collection("income")
            .document()
        docRef.set(income.toMap())
        return docRef.id
    }

    suspend fun getMonthlyIncome(uid: String, year: Int, month: Int): List<Income> {
        val startDate = "%04d-%02d-01".format(year, month)
        val endDate = if (month == 12) {
            "%04d-01-01".format(year + 1)
        } else {
            "%04d-%02d-01".format(year, month + 1)
        }

        val snapshot = try {
            db.collection("users").document(uid)
                .collection("income")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("income")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .await()
        }

        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { Income.fromMap(doc.id, it) }
        }
    }

    suspend fun softDeleteIncome(uid: String, incomeId: String) {
        db.collection("users").document(uid)
            .collection("income")
            .document(incomeId)
            .update("isDeleted", true)
            .await()
    }
}
