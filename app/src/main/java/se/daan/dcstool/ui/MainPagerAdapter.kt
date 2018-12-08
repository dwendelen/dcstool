package se.daan.dcstool.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MainPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    private val converterFragment = ConverterFragment()
    private val favoritesFragment = FavoritesFragment()
    private val converterFragment2 = ConverterFragment2()

    override fun getItem(p0: Int): Fragment {
        return when(p0) {
            0 -> converterFragment
            1 -> favoritesFragment
            2 -> converterFragment2
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getCount(): Int {
        return 3
    }
}