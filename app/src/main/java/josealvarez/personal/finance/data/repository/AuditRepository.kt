package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.AuditLog
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Query

class AuditRepository {

    private val db = FirestoreConfig.db

    suspend fun logAction(uid: String, log: AuditLog) {
        db.collection("users").document(uid)
            .collection("audit_log")
            .document()
            .set(log.toMap())
    }

    suspend fun getLogs(uid: String, limit: Long = 50): List<AuditLog> {
        val snapshot = db.collection("users").document(uid)
            .collection("audit_log")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get(Source.SERVER)
            .await()

        return snapshot.documents.map { doc ->
            AuditLog.fromMap(doc.id, doc.data ?: emptyMap())
        }
    }
}
