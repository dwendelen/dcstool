package daan.se.dcstool.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import daan.se.dcstool.R
import daan.se.dcstool.model.*
import daan.se.dcstool.model.parser.Parser
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import java.util.*

class ConverterFragment : Fragment() {
    private var subscriptions: List<Disposable> = emptyList()
    private val parser = Parser()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_converter, container, false)

        val input: TextView = view.findViewById(R.id.input)
        val spinner1: Spinner = view.findViewById(R.id.spinner1)
        val output1: TextView = view.findViewById(R.id.output1)
        val spinner2: Spinner = view.findViewById(R.id.spinner2)
        val output2: TextView = view.findViewById(R.id.output2)
        val spinner3: Spinner = view.findViewById(R.id.spinner3)
        val output3: TextView = view.findViewById(R.id.output3)

        val model: Model = ViewModelProviders.of(this).get(Model::class.java)
        input.text = model.input

        val inputChanges = RxTextView.textChanges(input)
        inputChanges.subscribe { newInput -> model.input = newInput }

        val parsed = inputChanges
                .map { parse(it) }
                .publish()

        parsed.subscribe { newCoordinate -> model.coordinate = newCoordinate.orElse(null) }

        val item1 = addGroup(view.context, parsed, spinner1, output1, model, 0)
        val item2 = addGroup(view.context, parsed, spinner2, output2, model, 1)
        val item3 = addGroup(view.context, parsed, spinner3, output3, model, 2)

        val parsedListener = parsed.connect()

        subscriptions = listOf(item1, item2, item3, parsedListener)

        return view
    }

    data class DropDownItem(val name: String, val factory: CoordinateFactory<*>) {
        override fun toString(): String {
            return name
        }
    }

    private val dropDownItems = listOf(
            DropDownItem("La Lo degrees", LaLoDegreeFactory),
            DropDownItem("La Lo minutes", LaLoMinuteFactory),
            DropDownItem("La Lo seconds", LaLoSecondFactory),
            DropDownItem("UTM", UTMFactory),
            DropDownItem("MGRS", MGRSFactory),
            DropDownItem("AJS-37", LaLoSecondFactory),
            DropDownItem("M-2000C", LaLoMinuteFactory),
            DropDownItem("F/A-18C", LaLoMinuteFactory)
    )

    private fun addGroup(context: Context, input: Observable<Optional<LaLoDegree>>, spinner: Spinner, output: TextView, model: Model, i: Int): Disposable {
        if (model.spinnerIdx[i] >= dropDownItems.size) {
            model.spinnerIdx[i] = 0
        }
        spinner.setSelection(model.spinnerIdx[i])
        spinner.adapter = ArrayAdapter<DropDownItem>(context, android.R.layout.simple_spinner_dropdown_item, dropDownItems)

        val selectedIndex = RxAdapterView.itemSelections(spinner)
        selectedIndex.subscribe { idx -> model.spinnerIdx[i] = idx }


        val factory = selectedIndex
                .map(dropDownItems::get)
                .map(DropDownItem::factory)

        return Observable.combineLatest(input, factory, BiFunction<Optional<LaLoDegree>, CoordinateFactory<*>, String> { inp, fac ->
            mapDegree(fac, inp)
        })
                .subscribe(output::setText)
    }

    private fun parse(input: CharSequence): Optional<LaLoDegree> {
        val coordinates = parser.parseChars(input)

        if (coordinates.size == 1) {
            return Optional.of(coordinates.iterator().next().toLaLoDegree())
        } else {
            if (coordinates.size > 1) {
                println("Found multiple: $coordinates") //TODO Remove
            }
            return Optional.empty()
        }
    }

    private fun mapDegree(factory: CoordinateFactory<*>, laLoDegree: Optional<LaLoDegree>): String {
        return laLoDegree
                .map(factory::fromLaLoDegree)
                .map(Coordinate::print)
                .orElse("")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
    }
}
