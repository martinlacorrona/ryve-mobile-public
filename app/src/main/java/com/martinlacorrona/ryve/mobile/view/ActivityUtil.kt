package com.martinlacorrona.ryve.mobile.view

import android.text.TextUtils

object ActivityUtil {
    @JvmStatic
    fun areNotEmpty(vararg elements: String?): Boolean {
        return elements.none(TextUtils::isEmpty)
    }
}