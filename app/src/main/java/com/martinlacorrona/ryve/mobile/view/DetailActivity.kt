package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.app.Properties
import com.martinlacorrona.ryve.mobile.databinding.ActivityDetailBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.HistoryActivity.Companion.HISTORY_ACTIVITY_FUEL_TYPE_ID
import com.martinlacorrona.ryve.mobile.view.HistoryActivity.Companion.HISTORY_ACTIVITY_STATION_SERVICE_ID
import com.martinlacorrona.ryve.mobile.viewmodel.DetailViewModel
import com.martinlacorrona.ryve.mobile.viewmodel.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : BaseActivity() {

    val TAG = "DetailActivity"

    private val vm: DetailViewModel by viewModels()
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.lifecycleOwner = this
        binding.vm = vm

        initTopBar()
        loadExtras()
        initBindings()
        initObservables()
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

    private fun loadExtras() {
        intent.extras?.let { bundle ->
            vm.stationServiceId.value = bundle.getLong(DETAIL_ACTIVITY_STATION_SERVICE_ID)
        }
        vm.getHistoryStationServiceEntity()?.apply {
            this@DetailActivity.title = stationServiceName
            vm.fuelId.value = fuelType.target.id
            vm.fuelName.value = fuelType.target.name
            vm.fuelPrice.value = price.toString()
        } ?: run {
            vm.fuelPrice.value = "-.--"
        }
    }

    private fun initBindings() {
        binding.buttonOpenInGoogleMaps.setOnClickListener {
            val gmmIntentUri = vm.stationService.value?.run {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($name)")
            }
            Intent(Intent.ACTION_VIEW, gmmIntentUri).let {
                it.setPackage("com.google.android.apps.maps")
                startActivity(it)
            }
        }
        binding.buttonAddAlert.setOnClickListener {
            addAlert()
        }
        binding.buttonHistorical.setOnClickListener {
            openHistoryActivity()
        }
        binding.buttonFavouriteAction.setOnClickListener {
            if(vm.isFavourite.value == false) {
                vm.addFavourite()
            } else {
                vm.deleteFavourite()
            }
        }
    }

    private fun initObservables() {
        vm.addFavouriteResource.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(getString(R.string.adding_to_favourites))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code != 200) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_favourite),
                                getString(R.string.error_ocurred_adding_favourite)
                            )
                        )
                    } else {
                        vm.isFavourite.value = true
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_favourite),
                                getString(R.string.add_succesfully)
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.add_favourite),
                            getString(R.string.error_connecting)
                        )
                    )
                }
            }
        }
        vm.removeFavouriteResource.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(getString(R.string.remove_to_favourites))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code != 200) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.remove_favourites),
                                getString(R.string.error_ocurred_removing_favourite)
                            )
                        )
                    } else {
                        vm.isFavourite.value = false
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.remove_favourites),
                                getString(R.string.remove_succesfully)
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.remove_favourites),
                            getString(R.string.error_connecting)
                        )
                    )
                }
            }
        }
        vm.addAlertResource.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(getString(R.string.adding_alert))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (it.code != 200) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_alert),
                                getString(R.string.error_ocurred_adding_alert)
                            )
                        )
                    } else {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_alert),
                                getString(R.string.add_alerta_succesfully)
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    if(it.code == -1)
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_alert),
                                getString(R.string.error_connecting)
                            )
                        )
                    else
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.add_alert),
                                it.message
                            )
                        )
                }
            }
        }
    }

    private fun addAlert() {
        val singleItems = arrayOf("1", "2", "3", "7", "14", "30")
        var checkedItem = 0
        var checkedItemValue = "1"

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.alert_every))
            .setNeutralButton(resources.getString(R.string.cancel), null)
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                vm.addAlert(checkedItemValue.toInt())
            }
            .setSingleChoiceItems(singleItems, checkedItem) { _, which ->
                checkedItem = which
                checkedItemValue = singleItems[which]
            }
            .show()
    }

    private fun openHistoryActivity() {
        Intent(this, HistoryActivity::class.java).apply {
            putExtra(HISTORY_ACTIVITY_STATION_SERVICE_ID, vm.stationServiceId.value)
            putExtra(HISTORY_ACTIVITY_FUEL_TYPE_ID, vm.fuelId.value)
            startActivity(this)
        }
    }

    companion object {
        const val DETAIL_ACTIVITY_STATION_SERVICE_ID = "DETAIL_ACTIVITY_STATION_SERVICE_ID"
    }
}