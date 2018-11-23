package daan.se.dcstool.ui

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import daan.se.dcstool.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.pager)
        viewPager.adapter = mainPagerAdapter
    }
}
