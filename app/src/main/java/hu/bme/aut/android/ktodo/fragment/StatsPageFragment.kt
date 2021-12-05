package hu.bme.aut.android.ktodo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import hu.bme.aut.android.ktodo.MainActivity
import hu.bme.aut.android.ktodo.data.todo.TodoProductivityTuple
import hu.bme.aut.android.ktodo.databinding.FragmentStatsPageBinding
import kotlin.concurrent.thread
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import hu.bme.aut.android.ktodo.R
import java.time.LocalDate


class StatsPageFragment(private val range: Range) : Fragment() {

    private lateinit var binding: FragmentStatsPageBinding
    private lateinit var chart: HorizontalBarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = binding.barChart
        chart.animateY(800)
        chart.setTouchEnabled(false)
        chart.setDrawValueAboveBar(false)
        chart.description.text = getString(R.string.chart_description)
        chart.description.textSize = 14f
        loadData()
    }

    /**
     * Loads the chart data.
     */
    private fun loadData() {
        thread {
            val items: List<TodoProductivityTuple>
            val rangeDays: Int
            if (range == Range.RANGE_7_DAYS) {
                items = MainActivity.database.todoItemDao().getProductivity7days()
                rangeDays = 7
            } else {
                items = MainActivity.database.todoItemDao().getProductivity30days()
                rangeDays = 30
            }
            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            for (n in 1..rangeDays) {
                val daysToSubtract = -rangeDays + n.toLong()
                val nextDayValue = LocalDate.now().plusDays(daysToSubtract)
                labels.add(nextDayValue.toString())
            }
            var i = 0
            for (label in labels) {
                val split = label.split("-")
                val date = LocalDate.of(split[0].toInt(), split[1].toInt(), split[2].toInt())
                val item = getItemWithDate(items, date)
                if (item != null) {
                    entries.add(BarEntry(i.toFloat(), item.count!!.toFloat()))
                } else {
                    entries.add(BarEntry(i.toFloat(), 0f))
                }
                i++
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            val dataSet = BarDataSet(entries, "Completed tasks")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 14f
            val barData = BarData(dataSet)
            barData.barWidth = 0.9f
            chart.data = barData
            chart.setFitBars(range == Range.RANGE_7_DAYS)
            chart.setPinchZoom(range == Range.RANGE_30_DAYS)
            chart.setScaleEnabled(range == Range.RANGE_30_DAYS)
            chart.setDrawGridBackground(false)
            chart.invalidate()
        }
    }

    private fun getItemWithDate(
        items: List<TodoProductivityTuple>,
        date: LocalDate
    ): TodoProductivityTuple? {
        for (item in items) {
            if (item.date?.toLocalDate() == date) {
                return item
            }
        }
        return null
    }

    companion object {
        enum class Range {
            RANGE_7_DAYS, RANGE_30_DAYS
        }
    }
}