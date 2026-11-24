package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.StatsPeriod
import com.Gumbatov.wallet.models.StatsState
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import com.Gumbatov.wallet.utils.StatsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/** State of the statistics screen: the chosen period and the calculated numbers. */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())

    private val period = MutableStateFlow(StatsPeriod.MONTH)

    val currentPeriod: StatsPeriod get() = period.value

    fun setPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod
    }

    val state: LiveData<StatsState> = combine(
        transactionRepository.transactions,
        categoryRepository.categories,
        period
    ) { transactions, categories, selectedPeriod ->
        StatsCalculator.calculate(
            transactions, categories, selectedPeriod, System.currentTimeMillis()
        )
    }.flowOn(Dispatchers.Default).asLiveData()
}