package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nextActivity()
    }

    private fun nextActivity() {
        if (vm.haveVersionDownloaded())
            Intent(this, UpdateActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(this)
            }
        else
            Intent(this, WelcomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(this)
            }
    }
}