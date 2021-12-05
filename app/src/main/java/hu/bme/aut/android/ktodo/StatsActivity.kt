package hu.bme.aut.android.ktodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hu.bme.aut.android.ktodo.databinding.ActivityStatsBinding
import hu.bme.aut.android.ktodo.fragment.StatsPageFragment

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var adapter: StatsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewPager = binding.statsViewPager
        viewPager.adapter = StatsPagerAdapter(supportFragmentManager)
        binding.tabs.setupWithViewPager(binding.statsViewPager)
        title = getString(R.string.stats_productivity_overview)
    }

    inner class StatsPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> StatsPageFragment(StatsPageFragment.Companion.Range.RANGE_7_DAYS)
                1 -> StatsPageFragment(StatsPageFragment.Companion.Range.RANGE_30_DAYS)
                else -> StatsPageFragment(StatsPageFragment.Companion.Range.RANGE_7_DAYS)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.stats_7_days)
                1 -> getString(R.string.stats_30_days)
                else -> ""
            }
        }
    }
}