package com.Gumbatov.wallet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Tag
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionTagCrossRef

@Database(
    entities = [Category::class, Transaction::class, Tag::class, TransactionTagCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagDao(): TransactionTagDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** Returns the single database instance, creating it on first use. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wallet.db"
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { instance = it }
            }
    }

    /** Runs once, when the database file is created: fills default categories. */
    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO categories (name, color, is_income) VALUES " +
                        "('Еда', '#FF9800', 0), " +
                        "('Транспорт', '#2196F3', 0), " +
                        "('Жильё', '#9C27B0', 0), " +
                        "('Развлечения', '#E91E63', 0), " +
                        "('Здоровье', '#4CAF50', 0), " +
                        "('Зарплата', '#009688', 1), " +
                        "('Подарки', '#FFC107', 1)"
            )
        }
    }
}