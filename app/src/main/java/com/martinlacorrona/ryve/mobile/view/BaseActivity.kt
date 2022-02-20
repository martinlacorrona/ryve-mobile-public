package com.martinlacorrona.ryve.mobile.view

import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.martinlacorrona.ryve.mobile.R

open class BaseActivity: AppCompatActivity() {

    private var _loadingDialog: MaterialDialog? = null
    private val loadingDialog: MaterialDialog
        get(){
            if(_loadingDialog==null){
                _loadingDialog = MaterialDialog.Builder(this)
                    .progress(true,0)
                    .cancelable(false)
                    .build()
            }
            return _loadingDialog!!
        }

    private var alertDialog: MaterialDialog? = null
        set(value){
            field?.dismiss()
            field = value
        }

    override fun onDestroy() {
        alertDialog?.dismiss()
        alertDialog?.dismiss()
        super.onDestroy()
    }

    fun setLoading(message: String) {
        loadingDialog.setContent(message)
        loadingDialog.show()
    }

    fun closeLoading() {
        loadingDialog.dismiss()
    }

    fun setAlertRetry(alertRetry: AlertRetry) {
        alertDialog?.dismiss()
        alertDialog = MaterialDialog.Builder(this).apply {
            canceledOnTouchOutside(false)
            alertRetry.title?.let { title(it) }
            alertRetry.content?.let { content(it) }

            positiveText(
                if (alertRetry.positiveText == null && alertRetry.onPositive != null) {
                    getString(R.string.retry)
                } else {
                    alertRetry.positiveText ?: getString(R.string.accept)
                }
            )
            onPositive { _, _ -> alertRetry.onPositive?.invoke() }

            val negativeText = if (alertRetry.negativeText == null
                && alertRetry.onPositive != null
                && alertRetry.positiveText == null) {
                getString(R.string.cancel)
            } else {
                alertRetry.negativeText
            }
            negativeText?.let { negativeText(it) }
            onNegative { _, _ -> alertRetry.onNegative?.invoke() }

            alertRetry.neutralText?.let { neutralText(it) }
            onNeutral { _, _ -> alertRetry.onNeutral?.invoke() }
        }.build()
        alertDialog?.show()
    }

    data class AlertRetry(
        var title: String? = null,
        var content: String? = null,
        var positiveText: String? = null,
        var onPositive: (() -> Unit)? = null,
        var negativeText: String? = null,
        var onNegative: (() -> Unit)? = null,
        var neutralText: String? = null,
        var onNeutral: (() -> Unit)? = null
    )

}