package com.weather.app.view.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weather.app.view.callbacks.OnOkButtonClickListener

class DialogUtils {
    companion object {
        fun showAlertDialog(
            context: Context,
            title: String,
            message: String,
            okBtnText: String,
            onOkButtonClickListener: OnOkButtonClickListener
        ) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle(title)
            alertDialog.setMessage(message)
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton(okBtnText) { _, _ ->
                onOkButtonClickListener.onOkButtonCLicked()
            }
            alertDialog.show()
        }
    }
}