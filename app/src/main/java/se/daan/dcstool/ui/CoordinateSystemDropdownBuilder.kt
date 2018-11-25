package se.daan.dcstool.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.Observable
import se.daan.dcstool.model.CoordinateFactory
import se.daan.dcstool.ui.model.CoordinateSystem
import se.daan.dcstool.ui.model.coordinateSystems
import se.daan.dcstool.ui.model.defaultCoordinateSystemIdx

object CoordinateSystemDropdownBuilder {
    fun build(context: Context, spinner: Spinner): Observable<CoordinateFactory<*>> {
        val dropDownItems = coordinateSystems
                .map { DropDownItem(it) }

        spinner.adapter = ArrayAdapter<DropDownItem>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                dropDownItems
        )
        spinner.setSelection(defaultCoordinateSystemIdx)

        return RxAdapterView.itemSelections(spinner)
                .map { coordinateSystems[it].factory }
    }

    data class DropDownItem(val coordinateSystem: CoordinateSystem) {
        override fun toString(): String {
            return coordinateSystem.name.toString()
        }
    }
}