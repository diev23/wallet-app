package com.Gumbatov.wallet.utils

import androidx.room.withTransaction
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.random.Random

/** Fills the database with random operations. For testing and for the screenshots of the user guide. */
object DemoData {

    private val expenseNames = listOf("Продукты", "Такси", "Кофе", "Кино", "Аптека", "Интернет", "Одежда")
    private val incomeNames = listOf("Зарплата", "Подработка", "Подарок")

    /** Adds [count] operations spread over the last 365 days. Runs in the background. */
    suspend fun generate(db: AppDatabase, count: Int) = withContext(Dispatchers.IO) {
        val repository = TransactionRepository(db)
        val categoryDao = db.categoryDao()

        db.withTransaction {
            // The generator needs at least one expense and one income category
            if (categoryDao.getAll().first().isEmpty()) {
                categoryDao.insert(Category(name = "Еда", color = "#FB8C00", isIncome = false))
                categoryDao.insert(Category(name = "Транспорт", color = "#1E88E5", isIncome = false))
                categoryDao.insert(Category(name = "Зарплата", color = "#43A047", isIncome = true))
            }
            val categories = categoryDao.getAll().first()
            val tags = db.tagDao().getAll().first()

            repeat(count) { index ->
                val category = categories.random()
                val names = if (category.isIncome) incomeNames else expenseNames
                val amount = if (category.isIncome) Random.nextLong(20_000, 300_000) else Random.nextLong(100, 50_000)
                val tagIds = tags.shuffled().take(Random.nextInt(0, 3)).map { it.id }
                repository.add(
                    Transaction(
                        title = "${names.random()} #${index + 1}",
                        description = "",
                        amountMinor = amount,
                        date = randomDay(),
                        categoryId = category.id
                    ),
                    tagIds
                )
            }
        }
    }

    /** A random day of the last year, at local midnight (like the date picker gives). */
    private fun randomDay(): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -Random.nextInt(0, 365))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}