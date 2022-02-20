package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.martinlacorrona.ryve.mobile.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }

    fun openLogin(@Suppress("UNUSED_PARAMETER")view: View) {
        Intent(this, LoginActivity::class.java).also { startActivity(it) }
    }

    fun openRegister(@Suppress("UNUSED_PARAMETER")view: View) {
        Intent(this, RegisterActivity::class.java).also { startActivity(it) }
    }
}