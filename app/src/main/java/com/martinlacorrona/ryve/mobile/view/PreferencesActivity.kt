package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.app.Properties
import com.martinlacorrona.ryve.mobile.databinding.ActivityPreferencesBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.UpdateActivity.Companion.FORCE_UPDATE
import com.martinlacorrona.ryve.mobile.viewmodel.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class PreferencesActivity : BaseActivity() {

    val TAG = "PreferencesActivity"

    private val vm: PreferencesViewModel by viewModels()
    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_preferences)
        binding.lifecycleOwner = this
        binding.vm = vm

        loadExtras()
        initTopBar()
        initObservers()
        initFuelTypeList()
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

    private fun loadExtras() {
        intent.extras?.get(Properties.MODE_PREFERENCES_ACTIVITY)?.let {
            vm.mode.value =
                it as PreferencesViewModel.PreferencesMode
        }
    }

    private fun initTopBar() {
        if (vm.mode.value == PreferencesViewModel.PreferencesMode.UPDATE) {
            setSupportActionBar(findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initFuelTypeList() {
        vm.fuelTypes.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(
                        getString(
                            R.string.loading,
                            getString(R.string.fuel_types).lowercase()
                        )
                    )
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code == 200) {
                        val items = it.data!!
                        val adapter = ArrayAdapter(applicationContext, R.layout.list_item, items)
                        binding.textFieldFavouriteFuelList.apply {
                            vm.favouriteFuel.value?.let {
                                setText(vm.favouriteFuel.value.toString())
                            }
                            setAdapter(adapter)
                            setOnItemClickListener { _, _, position, _ ->
                                vm.favouriteFuel.value = adapter.getItem(position)
                            }
                        }
                    } else {
                        errorLoadingFuelTypes()
                    }
                }
                Resource.Status.ERROR -> {
                    errorLoadingFuelTypes()
                }
            }
        }
    }

    private fun errorLoadingFuelTypes() {
        closeLoading()
        setAlertRetry(
            AlertRetry(
                getString(R.string.preferences),
                getString(
                    R.string.error_ocurred_loading,
                    getString(R.string.fuel_types).lowercase()
                )
            )
        )
        //Cargamos el que tenemos guardado, si lo tenemos claro
        (findViewById(R.id.text_field_favourite_fuel_list) as? AutoCompleteTextView)?.apply {
            vm.favouriteFuel.value?.let {
                setText(vm.favouriteFuel.value.toString())
            }
        }
    }

    private fun initObservers() {
        vm.userPreferences.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //ALERTA DE GUARDANDO
                    setLoading(
                        getString(
                            R.string.saving,
                            getString(R.string.preferences).lowercase()
                        )
                    )
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code == 200) {
                        if (vm.mode.value == PreferencesViewModel.PreferencesMode.CREATION) {
                            loadUpdateActivity()
                        } else {
                            //GUARDADO CORRECTO CORRECTO
                            setAlertRetry(
                                AlertRetry(
                                    getString(R.string.preferences),
                                    getString(
                                        R.string.save_succesfully
                                    ),
                                    positiveText = getString(R.string.ok),
                                    onPositive = {
                                        if (vm.isNeedToUpdateAgain())
                                            loadUpdateActivity()
                                        else
                                            finish()
                                    }
                                ),
                            )
                        }
                    } else {
                        //ALERTA DE ERROR GUARDANDO
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.preferences),
                                getString(
                                    R.string.error_ocurred_saving_with,
                                    getString(R.string.preferences).lowercase()
                                )
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    //ALERTA fallo guardando
                    if(it.code == -1)
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.preferences),
                                getString(
                                    R.string.error_ocurred_saving_with,
                                    getString(R.string.preferences).lowercase()
                                )
                            )
                        )
                    else
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.preferences),
                                it.message
                            )
                        )
                }
            }
        }
    }

    private fun loadUpdateActivity() {
        Intent(this, UpdateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(FORCE_UPDATE, true)
            startActivity(this)
        }
    }
}