package com.Gumbatov.wallet

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.viewmodels.TransactionListViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TEMPORARY: forces database creation so we can inspect it (remove on day 3)
        // TEMPORARY test code (remove at the end of day 3)
        val viewModel = ViewModelProvider(this)[TransactionListViewModel::class.java]

        viewModel.transactions.observe(this) { list ->
            Log.d("WalletTest", "Transactions in DB: ${list.size}")
            list.forEach {
                Log.d("WalletTest", "${it.transaction.title}, tags=${it.tags.map { tag -> tag.name }}")
            }
        }
        viewModel.error.observe(this) { message ->
            if (message != null) Log.e("WalletTest", message)
        }

// adds one test transaction to category 1 on every launch
        viewModel.add(
            Transaction(
                title = "Test lunch",
                amountMinor = 15000,
                date = System.currentTimeMillis(),
                categoryId = 1
            )
        )
    }
}