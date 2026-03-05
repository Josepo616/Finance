package josealvarez.personal.finance.data.repository

import josealvarez.personal.finance.FirestoreConfig
import josealvarez.personal.finance.model.Category
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source

class CategoryRepository {

    private val db = FirestoreConfig.db

    suspend fun getCategories(uid: String): List<Category> {
        val snapshot = try {
            db.collection("users").document(uid)
                .collection("categories")
                .whereEqualTo("isDeleted", false)
                .get(Source.SERVER)
                .await()
        } catch (e: Exception) {
            db.collection("users").document(uid)
                .collection("categories")
                .whereEqualTo("isDeleted", false)
                .get(Source.CACHE)
                .await()
        }

        return snapshot.documents.map { doc ->
            Category.fromMap(doc.id, doc.data ?: emptyMap())
        }
    }

    suspend fun addCategory(uid: String, category: Category): String {
        val docRef = db.collection("users").document(uid)
            .collection("categories")
            .document()
        docRef.set(category.toMap())
        return docRef.id
    }

    suspend fun updateCategory(uid: String, category: Category) {
        db.collection("users").document(uid)
            .collection("categories")
            .document(category.id)
            .set(category.toMap())
            .await()
    }

    suspend fun deleteCategory(uid: String, categoryId: String) {
        db.collection("users").document(uid)
            .collection("categories")
            .document(categoryId)
            .update("isDeleted", true)
            .await()
    }

    suspend fun seedDefaultCategories(uid: String) {
        val currentCategories = getCategories(uid)
        if (currentCategories.isEmpty()) {
            val batch = db.batch()
            Category.defaultCategories.forEach { category ->
                val docRef = db.collection("users").document(uid)
                    .collection("categories")
                    .document()
                batch.set(docRef, category.toMap())
            }
            batch.commit().await()
        }
    }

    suspend fun getCategoryById(uid: String, categoryId: String): Category? {
        val doc = db.collection("users").document(uid)
            .collection("categories")
            .document(categoryId)
            .get()
            .await()
        return doc.data?.let { Category.fromMap(doc.id, it) }
    }
}
