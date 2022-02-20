package com.martinlacorrona.ryve.mobile.app

import android.content.Context
import android.util.Log
import com.martinlacorrona.ryve.mobile.BuildConfig
import com.martinlacorrona.ryve.mobile.entity.MyObjectBox
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser


object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
        if (BuildConfig.DEBUG) {
            val started = AndroidObjectBrowser(boxStore).start(context)
            Log.i("ObjectBrowser", "Started: $started")
        }
    }
}