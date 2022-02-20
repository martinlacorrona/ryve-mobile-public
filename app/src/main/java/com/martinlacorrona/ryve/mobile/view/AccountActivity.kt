package com.martinlacorrona.ryve.mobile.view

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityAccountBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountActivity : BaseActivity() {

    private val vm: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_account)
        binding.lifecycleOwner = this
        binding.vm = vm

        initTopBar()
        initObservers()
        initBindings()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initTopBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initObservers() {
        vm.userResource.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //ALERTA ACUALIZANDO
                    setLoading(getString(R.string.updating_account_settings))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code != 200) {
                        //ALERTA DE ERROR ACTUALIZANDO
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.edit_account),
                                getString(R.string.error_ocurred_editing_account)
                            )
                        )
                    } else {
                        //ACTUALIZANDO CORRECTO
                        vm.saveCredentials()
                        finish()
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    //ALERTA DE ERROR ACTUALIZANDO SESION CONEXION
                    if (it.code == 400) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.edit_account),
                                it.message
                            )
                        )
                    } else {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.edit_account),
                                getString(R.string.error_connecting)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun initBindings() {
        binding.buttonSave.setOnClickListener {
            //Puede que las contraseñas esten vacias
            if (vm.password.value == "" && vm.repeatPassword.value == "") {
                vm.updateUser()
            } else {
                //Si no lo estan, deberan de coincidir
                if (vm.password.value != vm.repeatPassword.value) {
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.edit_account),
                            getString(R.string.password_are_not_same)
                        )
                    )
                } else {
                    vm.updateUser()
                }
            }
        }
    }
}