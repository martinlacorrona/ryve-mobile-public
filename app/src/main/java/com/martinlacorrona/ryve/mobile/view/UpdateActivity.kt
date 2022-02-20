package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityUpdateBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpdateActivity : BaseActivity() {

    private val vm: UpdateViewModel by viewModels()
    private lateinit var binding: ActivityUpdateBinding

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_update)
        binding.lifecycleOwner = this
        binding.vm = vm

        loadExtras()
        initObservers()
    }

    private fun loadExtras() {
        intent.extras?.getBoolean(FORCE_UPDATE)?.let {
            vm.forceUpdate = it
        }
    }

    private fun initObservers() {
        vm.user.observe(this) {
            if (it.status == Resource.Status.SUCCESS) {
                if (it.code == 401) { //Credenciales erroneas
                    vm.logout()
                    loadWelcomeActivity()
                } else if (it.code != 200) {
                    loadOffline()
                }
            } else if (it.status == Resource.Status.ERROR) {
                firebaseAnalytics.logEvent("update_server_error", Bundle())
                loadOffline()
            }
        }

        //Si cargo bien el principal
        vm.historyStationsService.observe(this) {
            if (it.status == Resource.Status.SUCCESS) {
                if (it.code == 200) {//Cargo y descargo
                    firebaseAnalytics.logEvent("update_screen_success", Bundle())
                    vm.saveLoaded()
                    loadPrincipalActivity()
                } else {
                    loadOffline()
                }
            } else if (it.status == Resource.Status.ERROR) {
                firebaseAnalytics.logEvent("update_server_error", Bundle())
                loadOffline()
            }
        }
    }

    private fun loadPrincipalActivity() {
        Intent(this, PrincipalActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(this)
        }
    }

    private fun loadWelcomeActivity() {
        Intent(this, WelcomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(this)
        }
    }

    private fun loadOffline() {
        if (vm.getLoaded()) { //Cargada version offline
            firebaseAnalytics.logEvent("load_offline", Bundle())
            setAlertRetry(
                AlertRetry(
                    getString(R.string.no_connection),
                    getString(R.string.load_offline),
                    onPositive = { loadPrincipalActivity() },
                    positiveText = getString(R.string.ok)
                )
            )
        } else { //Si no
            setAlertRetry(
                AlertRetry(
                    getString(R.string.no_connection),
                    getString(R.string.no_offline_version),
                    onPositive = { loadWelcomeActivity() },
                    positiveText = getString(R.string.ok)
                )
            )
        }
    }

    companion object {
        const val FORCE_UPDATE = "FORCE_UPDATE"
    }
}