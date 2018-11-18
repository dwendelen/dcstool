package daan.se.dcstool

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import daan.se.dcstool.model.*
import daan.se.dcstool.model.parser.Parser
import io.reactivex.disposables.Disposable
import java.util.*

class ConverterFragment : Fragment() {
    private var subscriptions: List<Disposable> = emptyList()
    private val parser = Parser()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_converter, container, false)

        val input: TextView = view.findViewById(R.id.input)
        val output1: TextView = view.findViewById(R.id.output1)
        val output2: TextView = view.findViewById(R.id.output2)
        val output3: TextView = view.findViewById(R.id.output3)

        val parsed = RxTextView.textChanges(input)
                .map { parse(it) }
                .publish()

        val laLoMin = parsed
                .map { mapDegree(LaLoMinuteFactory, it) }
                .subscribe(output1::setText)

        val laLoSec = parsed
                .map { mapDegree(LaLoSecondFactory, it) }
                .subscribe(output2::setText)

        val mgrs = parsed
                .map { mapDegree(MGRSFactory, it) }
                .subscribe(output3::setText)

        val parsedListener = parsed.connect()

        subscriptions = listOf(laLoMin, laLoSec, mgrs, parsedListener)

        return view
    }

    fun parse(input: CharSequence): Optional<LaLoDegree> {
        val coordinates = parser.parseChars(input)

        if (coordinates.size == 1) {
            return Optional.of(coordinates.iterator().next().toLaLoDegree())
        } else {
            if(coordinates.size > 1) {
                println("Found multiple: $coordinates") //TODO Remove
            }
            return Optional.empty()
        }
    }

    fun mapDegree(factory: CoordinateFactory<*>, laLoDegree: Optional<LaLoDegree>): String {
        return laLoDegree
                .map(factory::fromLaLoDegree)
                .map (Coordinate::print)
                .orElse("")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
    }
}
