package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityLoginBinding
import com.martinlacorrona.ryve.mobile.databinding.ActivityRegisterBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.LoginViewModel
import com.martinlacorrona.ryve.mobile.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private val vm: RegisterViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.lifecycleOwner = this
        binding.vm = vm

        initObservers()
        initBindings()
    }

    private fun initObservers() {
        vm.user.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //ALERTA DE REGISTRADOSE
                    setLoading(getString(R.string.registering))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code != 200) {
                        //ALERTA DE ERROR REGISTRANDOTE
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.register),
                                getString(R.string.error_ocurred_register)
                            )
                        )
                    } else {
                        //REGISTER CORRECTO
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle())
                        vm.saveCredentials()
                        Intent(this, PreferencesActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(this)
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    //ALERTA DE ERROR REGISTRADOSE SESION CONEXION
                    if(it.code == 400) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.register),
                                it.message
                            )
                        )
                    } else {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.register),
                                getString(R.string.error_connecting)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun initBindings() {
        binding.buttonRegister.setOnClickListener {
            if(vm.password.value != vm.repeatPassword.value) {
                setAlertRetry(
                    AlertRetry(
                        getString(R.string.register),
                        getString(R.string.password_are_not_same)
                    )
                )
            } else {
                vm.register()
            }
        }
    }
}