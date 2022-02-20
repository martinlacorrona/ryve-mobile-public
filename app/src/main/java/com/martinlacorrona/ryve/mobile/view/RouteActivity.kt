package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityRouteBinding
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.model.BoundsModel
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity.Companion.ROUTE_GENERATED_AVOID_TOLLS
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity.Companion.ROUTE_GENERATED_DESTINATION
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity.Companion.ROUTE_GENERATED_ORIGIN
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity.Companion.ROUTE_GENERATED_SELECTED_STATION_LAT_LNG
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity.Companion.ROUTE_GENERATED_SELECTED_STATION_LIST_NAME
import com.martinlacorrona.ryve.mobile.viewmodel.RouteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteActivity : BaseActivity(), OnMapReadyCallback {

    val TAG = "RouteActivity"
    private val vm: RouteViewModel by viewModels()
    private lateinit var binding: ActivityRouteBinding

    private lateinit var map: GoogleMap

    private var polylineDrew: MutableList<Polyline?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTopBar()
        initBindings()
        initObservables()
        loadExtras()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //Ponemos el centro
        val center = LatLng(40.4637, -3.0)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 5F))

        map.setOnMapLoadedCallback {
            //Limite de la ruta
            LatLngBounds(
                LatLng(vm.southwestBound.value?.lat!!, vm.southwestBound.value?.lng!!),
                LatLng(vm.northeastBound.value?.lat!!, vm.northeastBound.value?.lng!!)
            ).let {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(it, 200))
            }

            //Paints polylines route
            paintPolylines()
        }

        //Seteamos el ckick listener
        map.setOnMarkerClickListener { markerClicked ->
            (markerClicked.tag as HistoryStationServiceEntity).let {
                if (vm.selectedStationService.value?.get(it.id) == null) { //no esta seleccionada
                    vm.selectedStationService.value?.put(it.id, it)
                    vm.selectedStationService.postValue(vm.selectedStationService.value)
                    markerClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.selected_station))
                    Toast.makeText(
                        this,
                        "${getString(R.string.price)}: ${it.price} €",
                        Toast.LENGTH_SHORT
                    ).show()
                } else { //ya estaba seleccionada
                    vm.selectedStationService.value?.remove(it.id)
                    vm.selectedStationService.postValue(vm.selectedStationService.value)
                    markerClicked.setIcon(getIconFromHistoryStationService(it))
                }
            }
            true
        }
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

    private fun initBindings() {
        binding.buttonGenerateRoute.setOnClickListener {
            when {
                vm.haveDeadPoints -> {
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.you_have_dead_points),
                            getString(R.string.you_need_to_havent_dead_points)
                        )
                    )
                }
                vm.selectedStationService.value?.isEmpty() == true -> {
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.select_at_least),
                            getString(R.string.select_at_least_one_or_more)
                        )
                    )
                }
                else -> {
                    Intent(this, RouteGeneratedActivity::class.java).apply {
                        putExtra(ROUTE_GENERATED_ORIGIN, vm.origin)
                        putExtra(ROUTE_GENERATED_DESTINATION, vm.destination)

                        putExtra(
                            ROUTE_GENERATED_SELECTED_STATION_LIST_NAME,
                            vm.selectedStationServiceOrdered.value?.values?.map { "${it.stationServiceName}, ${it.stationService.target.address}" }
                                ?.toTypedArray()
                        )
                        putExtra(
                            ROUTE_GENERATED_SELECTED_STATION_LAT_LNG,
                            vm.selectedStationServiceOrdered.value?.values?.map {
                                RouteGeneratedActivity.LatLngSerializable(
                                    it.stationService.target.latitude,
                                    it.stationService.target.longitude
                                )
                            }
                                ?.toTypedArray()
                        )
                        putExtra(
                            RouteGeneratedActivity.ROUTE_GENERATED_TOTAL_DISTANCE,
                            vm.distanceInMeters.value
                        )
                        //Calculamos la media
                        var media = 0.0
                        vm.selectedStationService.value?.values?.forEach { media += it.price }
                        if (vm.selectedStationService.value?.size!! != 0)
                            media /= vm.selectedStationService.value?.size!!
                        putExtra(
                            //Al hacer el average se pierde el formato del numero
                            RouteGeneratedActivity.ROUTE_GENERATED_ESTIMATED_PRICE,
                            "%.2f".format(media)
                        )
                        putExtra(
                            RouteGeneratedActivity.ROUTE_GENERATED_PERCENTAGE_AT_ARRIVE,
                            "%.2f".format(vm.restPercentage)
                        )
                        putExtra(
                            RouteGeneratedActivity.ROUTE_GENERATED_ESTIMATED_TIME,
                            vm.durationInMinutes.value
                        )
                        putExtra(ROUTE_GENERATED_AVOID_TOLLS, vm.avoidTolls)
                        startActivity(this)
                    }
                }
            }
        }
    }

    private fun initObservables() {
        vm.possibleStationService.observe(this) {
            addItemsToListWithIcon(it)
        }
        vm.loadingPossibleStationService.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    closeLoading()
                    setLoading(getString(R.string.loading_near_station_services))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    if (vm.possibleStationService.value?.size!! == 0) {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.no_stations_near),
                                getString(R.string.select_other_route_no_stations_near),
                                onPositive = { finish() },
                                positiveText = getString(R.string.ok)
                            )
                        )
                    } else {
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.how_to_generate_route),
                                getString(R.string.instructions_generate_route)
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                }
            }
        }
        vm.routesPoints.observe(this) {
            paintPolylines()
        }
        vm.selectedStationService.observe(this) {
            it?.let {
                vm.calculateRouteByType()
            }
        }
    }

    private fun loadExtras() {
        intent.extras?.let { bundle ->
            vm.origin = bundle.get(ROUTE_ACTIVITY_ORIGIN) as LatLng?
            vm.destination = bundle.get(ROUTE_ACTIVITY_DESTINATION) as LatLng?
            vm.avoidTolls = bundle.getBoolean(ROUTE_ACTIVITY_AVOID_TOLLS)

            vm.maxPrice.value = bundle.getDouble(ROUTE_ACTIVITY_MAX_PRICE)
            vm.maxDistance.value = bundle.getDouble(ROUTE_ACTIVITY_MAX_DISTANCE)
            vm.percentageLoaded.value = bundle.getDouble(ROUTE_ACTIVITY_PERCENTAGE)

            vm.northeastBound.value = bundle.get(ROUTE_ACTIVITY_NORTHWEST_BOUND) as BoundsModel?
            vm.southwestBound.value = bundle.get(ROUTE_ACTIVITY_SOUTHWEST_BOUND) as BoundsModel?
            vm.distanceInMeters.value = bundle.get(ROUTE_ACTIVITY_DISTANCE_IN_METERS) as Int?
            vm.durationInMinutes.value = bundle.get(ROUTE_ACTIVITY_DURATION_IN_MINUTES) as Int?

            vm.route.value = PolyUtil.decode(bundle.getString(ROUTE_ACTIVITY_POINTS_ENCODED)!!)
        }
    }

    private fun paintPolylines() {
        polylineDrew.forEach { polyline -> polyline?.remove() }
        polylineDrew.clear()
        vm.routesPoints.value?.let {
            if (it.size > 0) {
                it.forEach { colorAndList ->
                    polylineDrew.add(
                        map.addPolyline(
                            PolylineOptions()
                                .color(getColor(colorAndList.color))
                                .addAll(colorAndList.list)
                        )
                    )
                }
            }
        }
    }

    /**
     * Añadimos las estaciones al cluster con su icono
     */
    private fun addItemsToListWithIcon(items: MutableList<HistoryStationServiceEntity>) {
        items.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.stationService.target.latitude,
                            it.stationService.target.longitude
                        )
                    )
                    .title(it.stationServiceName)
                    .icon(getIconFromHistoryStationService(it))
            )?.tag = it //aqui guardamos la estacion
        }
    }

    private fun getIconFromHistoryStationService(historyStationServiceEntity: HistoryStationServiceEntity): BitmapDescriptor {
        //Calcular rango caro, medio y caro para asi poner los iconos
        val minPrice = vm.getHistoryMinPrice()
        val maxPrice = vm.getHistoryMaxPrice()
        val difference = maxPrice - minPrice
        val differenceDividedIn5 = difference / 5.0
        val superExpensiveRangeMin = maxPrice - differenceDividedIn5
        val expensiveRangeMin = maxPrice - differenceDividedIn5 * 2
        val mediumRangeMin = maxPrice - differenceDividedIn5 * 3
        val lowRangeMin = maxPrice - differenceDividedIn5 * 4
        when {
            historyStationServiceEntity.price > superExpensiveRangeMin -> {
                return BitmapDescriptorFactory.fromResource(R.drawable.super_high_price)
            }
            historyStationServiceEntity.price > expensiveRangeMin -> {
                return BitmapDescriptorFactory.fromResource(R.drawable.high_price)
            }
            historyStationServiceEntity.price > mediumRangeMin -> {
                return BitmapDescriptorFactory.fromResource(R.drawable.medium_price)
            }
            historyStationServiceEntity.price > lowRangeMin -> {
                return BitmapDescriptorFactory.fromResource(R.drawable.low_price)
            }
            else -> {
                return BitmapDescriptorFactory.fromResource(R.drawable.super_low_price)
            }
        }
    }

    companion object {
        const val ROUTE_ACTIVITY_ORIGIN = "ROUTE_ACTIVITY_ORIGIN"
        const val ROUTE_ACTIVITY_DESTINATION = "ROUTE_ACTIVITY_DESTINATION"

        const val ROUTE_ACTIVITY_AVOID_TOLLS = "ROUTE_ACTIVITY_AVOID_TOLLS"

        const val ROUTE_ACTIVITY_MAX_PRICE = "ROUTE_ACTIVITY_MAX_PRICE"
        const val ROUTE_ACTIVITY_MAX_DISTANCE = "ROUTE_ACTIVITY_MAX_DISTANCE"
        const val ROUTE_ACTIVITY_PERCENTAGE = "ROUTE_ACTIVITY_PERCENTAGE"

        const val ROUTE_ACTIVITY_POINTS_ENCODED = "ROUTE_ACTIVITY_POINTS_ENCODED"
        const val ROUTE_ACTIVITY_NORTHWEST_BOUND = "ROUTE_ACTIVITY_NORTHWEST_BOUND"
        const val ROUTE_ACTIVITY_SOUTHWEST_BOUND = "ROUTE_ACTIVITY_SOUTHWEST_BOUND"
        const val ROUTE_ACTIVITY_DISTANCE_IN_METERS = "ROUTE_ACTIVITY_DISTANCE_IN_METERS"
        const val ROUTE_ACTIVITY_DURATION_IN_MINUTES = "ROUTE_ACTIVITY_DURATION_IN_MINUTES"
    }
}