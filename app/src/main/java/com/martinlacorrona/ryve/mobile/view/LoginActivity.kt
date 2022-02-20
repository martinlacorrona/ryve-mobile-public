package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityLoginBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val vm: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        binding.vm = vm

        initObservers()
    }

    private fun initObservers() {
        vm.user.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //ALERTA DE INICIANDO SESION
                    setLoading(getString(R.string.logining))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code == 401) {
                        //CREDENCIALES ERRONEAS
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.login),
                                getString(R.string.login_credentials_bad)
                            )
                        )
                    } else if (it.code != 200) {
                        //ALERTA DE ERROR INICIANDO SESION
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.login),
                                getString(R.string.error_ocurred)
                            )
                        )
                    } else {
                        //INICIAR SESION CORRECTAMENTE
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle())
                        vm.saveCredentials()
                        Intent(this, UpdateActivity::class.java).also {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(it)
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    //ALERTA DE ERROR INICIANDO SESION CONEXION
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.login),
                            getString(R.string.error_connecting)
                        )
                    )
                }
            }
        }
    }
}