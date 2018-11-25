package se.daan.dcstool.ui

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.Observable
import se.daan.dcstool.model.CoordinateFactory
import se.daan.dcstool.ui.model.coordinateSystems

object CoordinateSystemDropdownBuilder {
    fun build(context: Context, spinner: Spinner, initialIndex: Int): Observable<Pair<Int, CoordinateFactory<*>>> {
        spinner.setSelection(initialIndex)
        spinner.adapter = ArrayAdapter<ConverterFragment.DropDownItem>(context, R.layout.simple_spinner_dropdown_item, coordinateSystems.map { ConverterFragment.DropDownItem(it) })

        return RxAdapterView.itemSelections(spinner)
                .map { Pair(it, coordinateSystems[it].factory) }
    }
}