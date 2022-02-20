package com.martinlacorrona.ryve.mobile.viewmodel

import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.app.Properties
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.model.BoundsModel
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val stationServiceRepository: StationServiceRepository
) : BaseViewModel() {

    val TAG = "RouteViewModel"

    var origin: LatLng? = null
    var destination: LatLng? = null
    var avoidTolls: Boolean = false

    val route = MutableLiveData<MutableList<LatLng>>()
    val maxPrice = MutableLiveData<Double>().apply { value = 10.0 }
    val maxDistance = MutableLiveData<Double>().apply { value = 200.0 }
    val percentageLoaded = MutableLiveData<Double>().apply { value = 100.0 }
    val northeastBound = MutableLiveData<BoundsModel>()
    val southwestBound = MutableLiveData<BoundsModel>()
    val distanceInMeters = MutableLiveData<Int>()
    val durationInMinutes = MutableLiveData<Int>()

    val routesPoints =
        MediatorLiveData<MutableList<ColoredList>>().apply { value = mutableListOf() }
    var haveDeadPoints = true

    val possibleStationService =
        MutableLiveData<MutableList<HistoryStationServiceEntity>>().apply {
            value = mutableListOf()
        } //near stations
    val loadingPossibleStationService =
        MutableLiveData<Resource<Void>>()
    val selectedStationService =
        MediatorLiveData<MutableMap<Long, HistoryStationServiceEntity>>().apply {
            value = mutableMapOf()
        }
    val selectedStationServiceOrdered =
        MediatorLiveData<MutableMap<Long, HistoryStationServiceEntity>>().apply {
            value = mutableMapOf()
        }

    var restPercentage: Double? = null

    companion object {
        //Si tiene mayor que esto, sera para esa lista
        const val GOOD_PERCENTAGE = 0.7
        const val MEDIUM_PERCENTAGE = 0.3
        const val BAD_PERCENTAGE = 0.1

        const val GOOD_ID = R.color.good_route
        const val MEDIUM_ID = R.color.medium_route
        const val BAD_ID = R.color.bad_route
        const val DEAD_ID = R.color.dead_route
    }

    init {
        //Cuando se cambie la lista de route, añadimos las nuevas posibles estaciones
        routesPoints.addSource(route) {
            if (it.isNotEmpty()) { //Como no esta vacia buscamos las estaciones que esten en el rango
                stationServiceRepository.getHistoryInRangeByLatLongAndPriceByPoly(
                    route.value!!.toList(),
                    maxPrice.value ?: 10.0,
                    possibleStationService,
                    loadingPossibleStationService
                )
            }
        }
        //Cuando la lista esta a true es que ha cargado
        routesPoints.addSource(loadingPossibleStationService) {
            if (it.status == Resource.Status.SUCCESS) {
                //CALCULAMOS DE NUEVO
                calculateRouteByType()
            }
        }

        //Lo usamos para ordenar la lista
        selectedStationService.addSource(selectedStationService) {
            orderSelectedStations()
        }
    }

    fun calculateRouteByType() {
        routesPoints.value?.clear()

        //Distancia que nos quedaria, teniendo en cuanto el porcentaje que nos paso
        //Multicalmos por 1000 por que nos paso km, y nosotros queremos metros
        var distanceRest: Double? =
            percentageLoaded.value?.let { maxDistance.value!! * 1000.0 * (it / 100.0) }
        //Punto anterior, definimos el primero
        var previousPoint = route.value?.get(0)?.let { latlng ->
            Location("")
                .apply {
                    latitude = latlng.latitude
                    longitude = latlng.longitude
                }
        }
        //Le dejamos solo el 90% de capacidad, ya que no nos fiamos que el 100% sea real
        val maxDistanceInMeters = maxDistance.value!! * 1000 * 0.9
        var actualPointQuality = GOOD_ID
        var actualListQuality: MutableList<LatLng> = mutableListOf()

        haveDeadPoints = false //suponemos que no va a tener
        route.value?.forEach {
            //Restamos la distancia restante
            val newPoint = Location("")
                .apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            //Calculamos distancia restante
            distanceRest = distanceRest?.minus(
                previousPoint!!.distanceTo(newPoint).toDouble()
            )
            //Comprobamos si hay estacion al lado seleccionadas
            selectedStationService.value?.forEach {
                val stationServiceLatLgn = Location("")
                    .apply {
                        latitude = it.value.stationService.target.latitude
                        longitude = it.value.stationService.target.longitude
                    }

                //Multiplicamos por 2 para dar mas margen
                if (newPoint.distanceTo(stationServiceLatLgn) < Properties.DEFAULT_DISTANCE * 2) {
                    distanceRest = maxDistanceInMeters
                }
            }
            previousPoint = newPoint //guardamos el nuevo punto
            when {
                distanceRest?.div(maxDistanceInMeters)!! > GOOD_PERCENTAGE -> {
                    if (actualPointQuality == GOOD_ID) {
                        actualListQuality.add(it)
                    } else {
                        actualListQuality.add(it)
                        routesPoints.value?.add(ColoredList(actualPointQuality, actualListQuality))
                        actualListQuality = mutableListOf()
                        actualPointQuality = GOOD_ID
                        actualListQuality.add(it)
                    }
                }
                distanceRest?.div(maxDistanceInMeters)!! > MEDIUM_PERCENTAGE -> {
                    if (actualPointQuality == MEDIUM_ID) {
                        actualListQuality.add(it)
                    } else {
                        actualListQuality.add(it)
                        routesPoints.value?.add(ColoredList(actualPointQuality, actualListQuality))
                        actualListQuality = mutableListOf()
                        actualPointQuality = MEDIUM_ID
                        actualListQuality.add(it)
                    }
                }
                distanceRest?.div(maxDistanceInMeters)!! > BAD_PERCENTAGE -> {
                    if (actualPointQuality == BAD_ID) {
                        actualListQuality.add(it)
                    } else {
                        actualListQuality.add(it)
                        routesPoints.value?.add(ColoredList(actualPointQuality, actualListQuality))
                        actualListQuality = mutableListOf()
                        actualPointQuality = BAD_ID
                        actualListQuality.add(it)
                    }
                }
                else -> {
                    haveDeadPoints = true //tiene puntos muertos
                    if (actualPointQuality == DEAD_ID) {
                        actualListQuality.add(it)
                    } else {
                        actualListQuality.add(it)
                        routesPoints.value?.add(ColoredList(actualPointQuality, actualListQuality))
                        actualListQuality = mutableListOf()
                        actualPointQuality = DEAD_ID
                        actualListQuality.add(it)
                    }
                }
            }
        }
        restPercentage = distanceRest!! / maxDistanceInMeters * 100
        routesPoints.value?.add(ColoredList(actualPointQuality, actualListQuality))
        routesPoints.postValue(routesPoints.value)
    }

    private fun orderSelectedStations() {
        selectedStationServiceOrdered.value?.clear()
        route.value?.forEach {
            //Restamos la distancia restante
            val newPoint = Location("")
                .apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            //Comprobamos si hay estacion al lado seleccionadas
            selectedStationService.value?.forEach {
                val stationServiceLatLgn = Location("")
                    .apply {
                        latitude = it.value.stationService.target.latitude
                        longitude = it.value.stationService.target.longitude
                    }
                //Si esta cerca lo añadimos al hashmap
                if (newPoint.distanceTo(stationServiceLatLgn) < Properties.DEFAULT_DISTANCE * 2) {
                    selectedStationServiceOrdered.value?.putIfAbsent(it.key, it.value)
                }
            }
        }
    }

    fun getHistoryMinPrice(): Double = stationServiceRepository.getHistoryMinPrice()

    fun getHistoryMaxPrice(): Double = stationServiceRepository.getHistoryMaxPrice()

    data class ColoredList(
        var color: Int, //p.e. R.color.good
        var list: MutableList<LatLng>
    )
}