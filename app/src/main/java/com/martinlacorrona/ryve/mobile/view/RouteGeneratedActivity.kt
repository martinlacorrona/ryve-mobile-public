package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.model.LatLng
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityRouteGeneratedBinding
import com.martinlacorrona.ryve.mobile.viewmodel.RouteGeneratedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable


@AndroidEntryPoint
class RouteGeneratedActivity : AppCompatActivity() {

    val TAG = "RouteGeneratedActivity"

    private val vm: RouteGeneratedViewModel by viewModels()
    private lateinit var binding: ActivityRouteGeneratedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_route_generated)
        binding.lifecycleOwner = this
        binding.vm = vm

        loadExtras()
        initTopBar()
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

    private fun loadExtras() {
        intent.extras?.apply {
            vm.origin = get(ROUTE_GENERATED_ORIGIN) as LatLng?
            vm.destination = get(ROUTE_GENERATED_DESTINATION) as LatLng?
            vm.selectedStationServiceName =
                get(ROUTE_GENERATED_SELECTED_STATION_LIST_NAME) as Array<String>
            vm.selectedStationServiceLatLng =
                get(ROUTE_GENERATED_SELECTED_STATION_LAT_LNG) as Array<LatLngSerializable>
            vm.distance.value = "${getInt(ROUTE_GENERATED_TOTAL_DISTANCE) / 1000} km"
            vm.estimatedPrice.value = "${getString(ROUTE_GENERATED_ESTIMATED_PRICE)} €"
            vm.estimatedPercentageAtArrive.value =
                "${getString(ROUTE_GENERATED_PERCENTAGE_AT_ARRIVE)}%"
            vm.estimatedTime.value =
                DateUtils.formatElapsedTime(getInt(ROUTE_GENERATED_ESTIMATED_TIME).toLong())
            vm.avoidTolls = getBoolean(ROUTE_GENERATED_AVOID_TOLLS)
        }
    }

    private fun initTopBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initBindings() {
        binding.buttonImportToGoogleMaps.setOnClickListener {
            initGoogleMapsRouteIntent()
        }
        binding.stopsList.apply {
            adapter = ArrayAdapter(context, R.layout.list_item, vm.selectedStationServiceName)
        }
    }

    private fun initGoogleMapsRouteIntent() {
        var waypointsParsed = ""
        vm.selectedStationServiceLatLng.forEach {
            waypointsParsed += "$it|"
        }
        waypointsParsed.dropLast(1)
        var uri = "https://www.google.com/maps/dir/?api=1" +
                "&origin=${vm.origin?.formatted()}&destination=${vm.destination?.formatted()}" +
                "&waypoints=$waypointsParsed&travelmode=driving"
        /*if(vm.avoidTolls) {  //API cambiante, no es posiblem deberia de seleccionarlo el user
            uri += "/data=!4m3!4m2!2m1!2b1"
        }*/
        Uri.parse(uri).let {
            val intent = Intent(Intent.ACTION_VIEW, it)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    private fun LatLng.formatted() = "$latitude,$longitude"

    data class LatLngSerializable(
        var latitude: Double,
        var longitude: Double
    ) : Serializable {
        override fun toString(): String {
            return "$latitude,$longitude"
        }
    }


    companion object {
        const val ROUTE_GENERATED_ORIGIN = "ROUTE_GENERATED_ORIGIN"
        const val ROUTE_GENERATED_DESTINATION = "ROUTE_GENERATED_DESTINATION"
        const val ROUTE_GENERATED_SELECTED_STATION_LIST_NAME =
            "ROUTE_GENERATED_SELECTED_STATION_LIST_NAME"
        const val ROUTE_GENERATED_SELECTED_STATION_LAT_LNG =
            "ROUTE_GENERATED_SELECTED_STATION_LAT_LNG"
        const val ROUTE_GENERATED_TOTAL_DISTANCE = "ROUTE_GENERATED_TOTAL_DISTANCE"
        const val ROUTE_GENERATED_ESTIMATED_PRICE = "ROUTE_GENERATED_ESTIMATED_PRICE"
        const val ROUTE_GENERATED_PERCENTAGE_AT_ARRIVE = "ROUTE_GENERATED_PERCENTAGE_AT_ARRIVE"
        const val ROUTE_GENERATED_ESTIMATED_TIME = "ROUTE_GENERATED_ESTIMATED_TIME"
        const val ROUTE_GENERATED_AVOID_TOLLS = "ROUTE_GENERATED_AVOID_TOLLS"
    }
}