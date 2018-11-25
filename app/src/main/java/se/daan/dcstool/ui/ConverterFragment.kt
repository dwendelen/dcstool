package se.daan.dcstool.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import se.daan.dcstool.model.Coordinate
import se.daan.dcstool.model.CoordinateFactory
import se.daan.dcstool.model.LaLoDegree
import se.daan.dcstool.model.parser.Parser
import se.daan.dcstool.ui.model.Model
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
        val button: Button = view.findViewById(R.id.save_button)

        val model: Model = ViewModelProviders.of(activity!!).get(Model::class.java)
        input.text = model.input

        val inputChanges = RxTextView.textChanges(input)
        inputChanges.subscribe { newInput -> model.input = newInput }

        val parsed = inputChanges
                .map(this::parse)
                .publish()

        parsed.subscribe { newCoordinate -> model.coordinate = newCoordinate.orElse(null) }

        val item1 = addGroup(view.context, parsed, spinner1, output1)
        val item2 = addGroup(view.context, parsed, spinner2, output2)
        val item3 = addGroup(view.context, parsed, spinner3, output3)

        val parsedListener = parsed.connect()

        val buttonSubscription =
                RxView.clicks(button)
                        .subscribe {
                            val saveDialog = SaveDialog()
                            saveDialog.name
                                    .subscribe(model::saveCoordinate)
                            saveDialog.show(fragmentManager, "save_dialog")
                        }

        subscriptions = listOf(item1, item2, item3, listOf(parsedListener), listOf(buttonSubscription)).flatten()

        return view
    }

    private fun addGroup(context: Context, input: Observable<Optional<LaLoDegree>>, spinner: Spinner, output: TextView): List<Disposable> {
        val factory = CoordinateSystemDropdownBuilder.build(context, spinner)

        val textDisposable = Observable.combineLatest(input, factory, BiFunction<Optional<LaLoDegree>, CoordinateFactory<*>, String> { inp, fac ->
            mapDegree(fac, inp)
        }).subscribe(output::setText)

        return listOf(textDisposable)
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
        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
        super.onDestroyView()
    }
}
