package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.Expense
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source

class ExpenseRepository {

    private val db = FirestoreConfig.db

    suspend fun addExpense(uid: String, expense: Expense): String {
        val docRef = db.collection("users").document(uid)
            .collection("expenses")
            .document()
        docRef.set(expense.toMap())
        return docRef.id
    }

    suspend fun getMonthlyExpenses(uid: String, year: Int, month: Int): List<Expense> {
        val startDate = "%04d-%02d-01".format(year, month)
        val endDate = if (month == 12) {
            "%04d-01-01".format(year + 1)
        } else {
            "%04d-%02d-01".format(year, month + 1)
        }

        val snapshot = try {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .await()
        }

        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { Expense.fromMap(doc.id, it) }
        }
    }

    suspend fun getExpensesAfterDate(uid: String, startDate: String): List<Expense> {
        val snapshot = try {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereEqualTo("isDeleted", false)
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereEqualTo("isDeleted", false)
                .get(Source.CACHE)
                .await()
        }

        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { Expense.fromMap(doc.id, it) }
        }
    }

    suspend fun getExpensesInRange(uid: String, startDate: String, endDate: String): List<Expense> {
        val snapshot = try {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .whereEqualTo("isDeleted", false)
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .whereEqualTo("isDeleted", false)
                .get(Source.CACHE)
                .await()
        }

        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { Expense.fromMap(doc.id, it) }
        }
    }

    suspend fun softDeleteExpense(uid: String, expenseId: String) {
        db.collection("users").document(uid)
            .collection("expenses")
            .document(expenseId)
            .update("isDeleted", true)
            .await()
    }
}
