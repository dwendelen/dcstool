package se.daan.dcstool.ui.model;

import android.arch.lifecycle.ViewModel
import se.daan.dcstool.model.LaLoDegree

class Model : ViewModel() {
    var input: CharSequence = ""
    var coordinate: LaLoDegree? = null
    val spinnerIdx: Array<Int> = arrayOf(0, 0, 0)
}
