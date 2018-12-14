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
    private var keySubscriptions: List<Disposable> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_converter2, container, false)

        val input: TextView = view.findViewById(R.id.input22)
        val spinner1: Spinner = view.findViewById(R.id.spinner22)
        val output1: TextView = view.findViewById(R.id.output22)
        val button: Button = view.findViewById(R.id.save_button22)

        val model: Model = ViewModelProviders.of(activity!!).get(Model::class.java)
        val states = getStates(view, model)

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

        val inputSubscription = states.subscribe({ state ->
            input.text = state.print()
        }, {
            it.printStackTrace()
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
            view: View,
            model: Model
    ): Observable<ParserState> {
        val subject = PublishSubject.create<ParserState>()


        var onKeyPressed: (Key, CharSequence) -> Unit = {_,_ ->}
        onKeyPressed = { key, preferredKeyboard ->
            when (key) {
                is BackKey -> model.stack.remove()
                is InputKey -> {
                    val newState = model.parserState.handle(key.input)
                    model.stack.addFirst(newState)
                }
            }

            renderKeyboards(view, model, onKeyPressed, preferredKeyboard)
            subject.onNext(model.parserState)
        }

        renderKeyboards(view, model, onKeyPressed, "")

        val replay = subject.replay(1)
        replay.connect()
        subject.onNext(model.parserState)
        return replay
    }

    private fun renderKeyboards(
            view: View,
            model: Model,
            onKeyPressed: (Key, CharSequence) -> Unit,
            preferredKeyboard: CharSequence
    ) {
        val state = model.parserState
        val inputs = state.inputs
        val keyboards = getKeyboards(inputs)
        val idx = keyboards.indexOfFirst { it.id == preferredKeyboard }
                .let { if (it == -1) 0 else it }

        val disableBack = model.stack.size <= 1

        renderKeyboards(view, keyboards, idx, disableBack, onKeyPressed)
    }

    private fun renderKeyboards(
            view: View,
            keyboards: List<Keyboard>,
            idx: Int,
            disableBack: Boolean,
            onKeyPressed: (Key, CharSequence) -> Unit
    ) {
        val disableMode = keyboards.size <= 1
        val keyboard = keyboards[idx]

        val myOnKeyPressed: (Key) -> Unit = { key ->
            keySubscriptions.forEach { it.dispose() }

            if (key is ModeKey) {
                val newIdx = (idx + 1) % keyboards.size
                renderKeyboards(view, keyboards, newIdx, disableBack, onKeyPressed)
            } else {
                onKeyPressed(key, keyboard.id)
            }
        }

        keySubscriptions = renderKeyboard(view, keyboard, disableMode, disableBack, myOnKeyPressed)
    }

    private fun renderKeyboard(
            view: View,
            keyboard: Keyboard,
            disableMode: Boolean,
            disableBack: Boolean,
            onKeyPressed: (Key) -> Unit
    ): List<Disposable> {
        return listOfNotNull(
                renderKey(view, R.id.key11, keyboard, 0, 0, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key12, keyboard, 0, 1, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key13, keyboard, 0, 2, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key14, keyboard, 0, 3, disableMode, disableBack, onKeyPressed),

                renderKey(view, R.id.key21, keyboard, 1, 0, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key22, keyboard, 1, 1, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key23, keyboard, 1, 2, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key24, keyboard, 1, 3, disableMode, disableBack, onKeyPressed),

                renderKey(view, R.id.key31, keyboard, 2, 0, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key32, keyboard, 2, 1, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key33, keyboard, 2, 2, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key34, keyboard, 2, 3, disableMode, disableBack, onKeyPressed),

                renderKey(view, R.id.key41, keyboard, 3, 0, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key42, keyboard, 3, 1, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key43, keyboard, 3, 2, disableMode, disableBack, onKeyPressed),
                renderKey(view, R.id.key44, keyboard, 3, 3, disableMode, disableBack, onKeyPressed)
        )
    }

    private fun renderKey(
            view: View,
            buttonId: Int,
            keyboard: Keyboard,
            row: Int,
            keyIdx: Int,
            disableMode: Boolean,
            disableBack: Boolean,
            onKeyPressed: (Key) -> Unit
    ): Disposable? {
        val button: Button = view.findViewById(buttonId)
        val key = keyboard.rows[row].keys[keyIdx]

        return renderKey(button, key, disableMode, disableBack, onKeyPressed)
    }

    private fun renderKey(
            button: Button,
            key: Key,
            disableMode: Boolean,
            disableBack: Boolean,
            onKeyPressed: (Key) -> Unit
    ): Disposable? {
        button.text = key.text

        button.isEnabled = when (key) {
            is DisabledKey -> false
            is EmptyKey -> false
            is InputKey -> true
            is BackKey -> !disableBack
            is ModeKey -> !disableMode
        }

        return if (button.isEnabled) {
            RxView.clicks(button).subscribe { onKeyPressed(key) }
        } else {
            null
        }
    }

    override fun onDestroyView() {
        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
        keySubscriptions.forEach(Disposable::dispose)
        keySubscriptions = emptyList()
        super.onDestroyView()
    }
}
