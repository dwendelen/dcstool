package se.daan.dcstool.ui

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main)

        val mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.pager)
        viewPager.adapter = mainPagerAdapter
    }
}
