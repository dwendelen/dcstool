package daan.se.dcstool.ui;

import android.arch.lifecycle.ViewModel
import daan.se.dcstool.model.LaLoDegree

class Model : ViewModel() {
    var input: CharSequence = ""
    var coordinate: LaLoDegree? = null
    val spinnerIdx: Array<Int> = arrayOf(0, 0, 0)
}
