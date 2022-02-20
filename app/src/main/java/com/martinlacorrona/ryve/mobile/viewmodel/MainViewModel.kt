package com.martinlacorrona.ryve.mobile.viewmodel

import android.content.SharedPreferences
import com.martinlacorrona.ryve.mobile.app.Properties.LOADED
import com.martinlacorrona.ryve.mobile.app.Properties.PREFERENCE_USER
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    val TAG = "MainViewModel"

    fun haveVersionDownloaded(): Boolean {
        return sharedPreferences.getBoolean(LOADED, false)
    }
}