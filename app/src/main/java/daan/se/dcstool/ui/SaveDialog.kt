package daan.se.dcstool.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.EditText
import daan.se.dcstool.R
import io.reactivex.subjects.MaybeSubject

class SaveDialog : DialogFragment() {
    val name: MaybeSubject<CharSequence> = MaybeSubject.create()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflated = it.layoutInflater.inflate(R.layout.save_dialog, null)
            val input: EditText = inflated.findViewById(R.id.save_dialog_input)

            builder.setMessage("Save coordinate")
                    .setView(inflated)
                    .setPositiveButton("Save") { _, _ ->
                        name.onSuccess(input.text)
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        name.onComplete()
                    }
            builder.create()
        }?: throw IllegalStateException("Activity can not be null")
    }
}