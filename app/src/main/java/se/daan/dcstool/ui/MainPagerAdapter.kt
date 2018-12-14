package se.daan.dcstool.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MainPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    private val converterFragment = ConverterFragment()
    private val favoritesFragment = FavoritesFragment()

    override fun getItem(p0: Int): Fragment {
        return when(p0) {
            0 -> converterFragment
            1 -> favoritesFragment
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}