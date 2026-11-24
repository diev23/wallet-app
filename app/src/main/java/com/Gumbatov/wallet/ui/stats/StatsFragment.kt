package com.Gumbatov.wallet.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.databinding.FragmentStatsBinding
import com.Gumbatov.wallet.models.StatsPeriod
import com.Gumbatov.wallet.models.StatsState
import com.Gumbatov.wallet.utils.Formatters
import com.Gumbatov.wallet.viewmodels.StatsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.color.MaterialColors

/** Statistics screen: numbers, a pie chart of expenses and a bar chart by months. */
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupCharts()

        binding.periodToggle.check(
            when (viewModel.currentPeriod) {
                StatsPeriod.WEEK -> R.id.period_week
                StatsPeriod.MONTH -> R.id.period_month
                StatsPeriod.YEAR -> R.id.period_year
            }
        )
        binding.periodToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setPeriod(
                    when (checkedId) {
                        R.id.period_week -> StatsPeriod.WEEK
                        R.id.period_year -> StatsPeriod.YEAR
                        else -> StatsPeriod.MONTH
                    }
                )
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state) }
    }

    /** Color of the text on charts: follows the light / dark theme. */
    private fun textColor(): Int =
        MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurface)

    private fun setupCharts() {
        val textColor = textColor()

        binding.pieChart.apply {
            description.isEnabled = false
            setNoDataText(getString(R.string.stats_no_expenses))
            setNoDataTextColor(textColor)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setCenterTextColor(textColor)
            legend.textColor = textColor
            legend.isWordWrapEnabled = true
        }

        binding.barChart.apply {
            description.isEnabled = false
            setNoDataTextColor(textColor)
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.textColor = textColor
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = textColor
            legend.textColor = textColor
            setScaleEnabled(false)
        }
    }

    private fun render(state: StatsState) {
        val hasData = state.transactionCount > 0
        binding.statsContent.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.statsEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        if (!hasData) return

        binding.incomeValue.text = Formatters.formatMoney(state.incomeMinor)
        binding.expenseValue.text = Formatters.formatMoney(state.expenseMinor)
        binding.balanceValue.text = Formatters.formatMoney(state.balanceMinor)
        binding.averageValue.text = Formatters.formatMoney(state.averageExpensePerDayMinor)
        binding.incomeValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.income))
        binding.expenseValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense))
        binding.balanceValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (state.balanceMinor >= 0) R.color.income else R.color.expense
            )
        )

        renderPie(state)
        renderBars(state)
    }

    private fun renderPie(state: StatsState) {
        val chart = binding.pieChart
        if (state.expenseByCategory.isEmpty()) {
            chart.clear() // shows the "no expenses" text
            return
        }

        val entries = state.expenseByCategory.map {
            PieEntry(it.amountMinor / 100f, it.category.name)
        }
        val sliceColors = state.expenseByCategory.map { parseColor(it.category.color) }

        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(sliceColors)
        dataSet.sliceSpace = 2f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(chart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.WHITE)

        chart.data = pieData
        chart.centerText = Formatters.formatMoney(state.expenseMinor)
        chart.invalidate()
    }

    private fun renderBars(state: StatsState) {
        val months = state.months
        val incomeEntries = months.mapIndexed { i, m -> BarEntry(i.toFloat(), m.incomeMinor / 100f) }
        val expenseEntries = months.mapIndexed { i, m -> BarEntry(i.toFloat(), m.expenseMinor / 100f) }

        val incomeSet = BarDataSet(incomeEntries, getString(R.string.stats_income))
        incomeSet.color = ContextCompat.getColor(requireContext(), R.color.income)
        incomeSet.setDrawValues(false)

        val expenseSet = BarDataSet(expenseEntries, getString(R.string.stats_expense))
        expenseSet.color = ContextCompat.getColor(requireContext(), R.color.expense)
        expenseSet.setDrawValues(false)

        val barData = BarData(incomeSet, expenseSet)
        barData.barWidth = 0.4f

        val chart = binding.barChart
        chart.data = barData
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(months.map { it.label })
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.axisMaximum = months.size.toFloat()
        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.labelCount = months.size
        // group = 2 * (0.4 + 0.05) + 0.1 = 1.0, as the library requires
        chart.groupBars(0f, 0.1f, 0.05f)
        chart.invalidate()
    }

    private fun parseColor(hex: String): Int =
        runCatching { Color.parseColor(hex) }.getOrDefault(Color.GRAY)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}