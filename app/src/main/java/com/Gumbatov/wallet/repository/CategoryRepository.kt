package com.Gumbatov.wallet.repository

import com.Gumbatov.wallet.database.CategoryDao
import com.Gumbatov.wallet.models.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    val categories: Flow<List<Category>> = dao.getAll()

    suspend fun add(category: Category): Long = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)

    /** Deleting a category also deletes all its transactions (ON DELETE CASCADE). */
    suspend fun delete(category: Category) = dao.delete(category)
}