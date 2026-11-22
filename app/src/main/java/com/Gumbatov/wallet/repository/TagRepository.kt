package com.Gumbatov.wallet.repository

import com.Gumbatov.wallet.database.TagDao
import com.Gumbatov.wallet.models.Tag
import kotlinx.coroutines.flow.Flow

class TagRepository(private val dao: TagDao) {
    val tags: Flow<List<Tag>> = dao.getAll()

    suspend fun add(tag: Tag): Long = dao.insert(tag)
    suspend fun delete(tag: Tag) = dao.delete(tag)
}