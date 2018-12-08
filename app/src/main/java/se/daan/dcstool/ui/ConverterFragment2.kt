package se.daan.dcstool.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import se.daan.dcstool.model.Coordinate
import se.daan.dcstool.model.CoordinateFactory
import se.daan.dcstool.model.parser.ParserState
import se.daan.dcstool.ui.model.*
import java.util.*


class ConverterFragment2 : Fragment() {
    private var subscriptions: List<Disposable> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_converter2, container, false)

        val input: TextView = view.findViewById(R.id.input22)
        val spinner1: Spinner = view.findViewById(R.id.spinner22)
        val output1: TextView = view.findViewById(R.id.output22)
        val keyboard: TableLayout = view.findViewById(R.id.keyboard22)
        val button: Button = view.findViewById(R.id.save_button22)

        val model: Model = ViewModelProviders.of(activity!!).get(Model::class.java)
        val states = getStates(view.context, keyboard, model)

        val outputSubscriptions = connectOutputs(view.context, states, input, spinner1, output1)

        val buttonSubscription =
                RxView.clicks(button)
                        .subscribe {
                            val saveDialog = SaveDialog()
                            saveDialog.name
                                    .subscribe(model::saveCoordinate2)
                            saveDialog.show(fragmentManager, "save_dialog")
                        }

        subscriptions = listOf(outputSubscriptions, listOf(buttonSubscription)).flatten()

        return view
    }

    private fun connectOutputs(
            context: Context,
            states: Observable<ParserState>,
            input: TextView,
            spinner: Spinner,
            output: TextView
    ): List<Disposable> {
        val factory = CoordinateSystemDropdownBuilder.build(context, spinner)

        val inputSubscription = states.subscribe( { state ->
            input.text = state.print()
        }, {
            it.printStackTrace()
        }, {
            println ("comp")
        })

        val outputSubscription = Observable.combineLatest(states, factory, BiFunction<ParserState, CoordinateFactory<*>, String> { inp, fac ->
            mapDegree(fac, inp)
        }).subscribe(output::setText)

        return listOf(inputSubscription, outputSubscription)
    }


    private fun mapDegree(factory: CoordinateFactory<*>, state: ParserState): String {
        return Optional.ofNullable(state.coordinate)
                .map(Coordinate::toLaLoDegree)
                .map(factory::fromLaLoDegree)
                .map(Coordinate::print)
                .orElse("")
    }

    private fun getStates(
            context: Context,
            table: TableLayout,
            model: Model
    ):  Observable<ParserState> {
        val state = model.parserState
        val inputs = state.inputs
        val keyboards = getKeyboards(inputs)
        val idx = 0

        val next = getKeys(context, table, keyboards, idx, model.stack.size <= 1)
                .flatMap {
                    when(it) {
                        is BackKey -> model.stack.remove()
                        is InputKey -> {
                            val newState = state.handle(it.input)
                            model.stack.addFirst(newState)
                        }
                    }

                    getStates(context, table, model)
                }
                .share()

        return Observable.concat(
                Observable.fromArray(state),
                next
        )
    }

    private fun getKeys(
            context: Context,
            table: TableLayout,
            keyboards: List<Keyboard>,
            idx: Int,
            disableBack: Boolean
    ): Observable<Key> {
        val disableMode = keyboards.size <= 1
        val keyboard = keyboards[idx]
        val keys = renderKeyboard(context, table, keyboard, disableMode, disableBack)

        return keys.flatMap { key ->
            if(key is ModeKey) {
                val newIdx = (idx + 1)%keyboards.size
                getKeys(context, table, keyboards, newIdx, disableBack)
            } else {
                Observable.fromArray(key)
            }
        }
    }

    private fun renderKeyboard(
            context: Context,
            table: TableLayout,
            keyboard: Keyboard,
            disableMode: Boolean,
            disableBack: Boolean
    ): Observable<Key> {
        table.removeAllViews()

        val tableRows = keyboard.rows
                .map {
                    renderRow(
                            context,
                            it,
                            disableMode,
                            disableBack
                    )
                }

        tableRows.forEach {
            table.addView(it.first, TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }

        val observable = Observable.merge(
                tableRows.map { it.second }
        )

        return observable
    }

    private fun renderRow(
            context: Context,
            row: Row,
            disableMode: Boolean,
            disableBack: Boolean
    ): Pair<TableRow, Observable<Key>> {
        val tableRow = TableRow(context)
        tableRow.setPadding(0, 0, 0, 0)

        val keys = row.keys
                .map { renderKey(context, it, disableMode, disableBack) }

        keys.forEach {
            tableRow.addView(it.first, TableRow.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT
            ))
        }

        val observable = Observable.merge(
                keys.map { it.second }
        )

        return Pair(tableRow, observable)
    }

    private fun renderKey(
            context: Context,
            key: Key,
            disableMode: Boolean,
            disableBack: Boolean
    ): Pair<Button, Observable<Key>> {
        val button = Button(context)
        button.text = key.text
        val textSize = 60f

        when (key) {
            is DisabledKey -> {
                button.isEnabled = false
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            }
            is EmptyKey ->
                button.isEnabled = false
            is InputKey ->
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            is BackKey -> {
                if (disableBack) {
                    button.isEnabled = false
                }
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            }
            is ModeKey -> {
                if (disableMode) {
                    button.isEnabled = false
                }
            }
        }

        val observable = if (button.isEnabled) {
            RxView.clicks(button)
                    .map { key }
        } else {
            Observable.empty()
        }

        return Pair(button, observable)
    }

    override fun onDestroyView() {
        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
        super.onDestroyView()
    }
}
