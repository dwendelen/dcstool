package daan.se.dcstool

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.pager)
        viewPager.adapter = mainPagerAdapter
    }
}
