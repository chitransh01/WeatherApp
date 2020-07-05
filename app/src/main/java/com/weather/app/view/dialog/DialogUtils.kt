package com.weather.app.view.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weather.app.view.callbacks.OnOkButtonClickListener

class DialogUtils {
    companion object {
        /**
         * This method is used to show alert dialog
         *
         * @param context This param will be the context of your activity
         * @param title This param will be the title of alert dialog
         * @param message This param will be the message to be show on the alert dialog
         * @param okBtnText This param will be the title of alert dialog actio button
         * @param onOkButtonClickListener This param will be the interface which will give us callback to handle click event
         */
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