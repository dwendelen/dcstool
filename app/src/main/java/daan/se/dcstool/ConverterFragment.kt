package daan.se.dcstool

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.Disposable

class ConverterFragment : Fragment() {
    private lateinit var subscription: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_converter, container, false)

        val input: TextView = view.findViewById(R.id.input)

        subscription = RxTextView.textChangeEvents(input)
                .subscribe { e -> println(e.text())}

        return view
    }
}
