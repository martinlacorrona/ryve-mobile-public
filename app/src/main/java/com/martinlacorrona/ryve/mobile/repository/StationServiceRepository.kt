package com.martinlacorrona.ryve.mobile.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinlacorrona.ryve.mobile.app.ObjectBox
import com.martinlacorrona.ryve.mobile.app.Properties.DEFAULT_DISTANCE
import com.martinlacorrona.ryve.mobile.entity.*
import com.martinlacorrona.ryve.mobile.model.*
import com.martinlacorrona.ryve.mobile.repository.util.GeoDistanceAlgorithm
import com.martinlacorrona.ryve.mobile.rest.FuelTypeService
import com.martinlacorrona.ryve.mobile.rest.StationServiceService
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.FilterViewModel
import io.objectbox.android.ObjectBoxDataSource
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StationServiceRepository @Inject constructor(
    private val stationServiceService: StationServiceService,
    private val fuelTypeService: FuelTypeService,
    private val gson: Gson
) {

    val TAG = "StationServiceRepository"

    private val historyStationServiceBox = ObjectBox.boxStore.boxFor<HistoryStationServiceEntity>()
    private val fuelTypesBox = ObjectBox.boxStore.boxFor<FuelTypeEntity>()
    private val stationServiceBox = ObjectBox.boxStore.boxFor<StationServiceEntity>()

    fun updateHistoryStationService(
        result: MediatorLiveData<Resource<List<HistoryStationServiceModel>>>,
        fuelTypeId: Long?
    ) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    stationServiceService.getStationServiceByFuelId(fuelTypeId = fuelTypeId!!)
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        historyStationServiceBox.removeAll()
                        historyStationServiceBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun getHistoryStationServiceByFilter(filterModel: FilterModel, pageSize: Int = 20):
            LiveData<PagedList<HistoryStationServiceEntity>> {
        return LivePagedListBuilder(
            ObjectBoxDataSource.Factory(queryByFilter(filterModel).build()),
            pageSize
        ).build()
    }

    private fun queryByFilter(filterModel: FilterModel): QueryBuilder<HistoryStationServiceEntity> {
        val orderBy = filterModel.orderBy.let {
            if (it == FilterViewModel.OrderBy.PRICE)
                HistoryStationServiceEntity_.price
            else
                HistoryStationServiceEntity_.stationServiceName
        }
        val orderWay = filterModel.orderWay.let {
            if (it == FilterViewModel.OrderWay.ASC)
                QueryBuilder.CASE_SENSITIVE
            else
                QueryBuilder.DESCENDING
        }
        val query: QueryBuilder<HistoryStationServiceEntity> =
            historyStationServiceBox.query()
                .greater(
                    HistoryStationServiceEntity_.price,
                    filterModel.priceMin ?: Double.MIN_VALUE
                )
                .less(HistoryStationServiceEntity_.price, filterModel.priceMax ?: Double.MAX_VALUE)
                .eager(HistoryStationServiceEntity_.stationService)
                .order(orderBy, orderWay)

        filterModel.ccaaId?.let {
            if(it != 0L) {
                query.equal(HistoryStationServiceEntity_.idCCAA, it)
            }
        }
        return query
    }


    fun getHistoryStationServiceByName(name: String, filterModel: FilterModel, pageSize: Int = 20):
            LiveData<PagedList<HistoryStationServiceEntity>> {
        Log.d(TAG, "getHistoryStationServiceByName: $name")
        val query = queryByFilter(filterModel)
        query.apply {
                link(HistoryStationServiceEntity_.stationService).contains(
                    StationServiceEntity_.name,
                    name
                )
            }
        query.eager(HistoryStationServiceEntity_.stationService)
        query.order(HistoryStationServiceEntity_.price)
        return LivePagedListBuilder(
            ObjectBoxDataSource.Factory(query.build()),
            pageSize
        ).build()
    }

    fun updateFuelTypes(): MediatorLiveData<Resource<List<FuelTypeModel>>> {
        val result = MediatorLiveData<Resource<List<FuelTypeModel>>>()
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = fuelTypeService.getFuels()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        fuelTypesBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
        return result
    }

    fun updateStationService(result: MediatorLiveData<Resource<List<StationServiceModel>>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = stationServiceService.getStationService()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        stationServiceBox.removeAll()
                        stationServiceBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun getStationService(stationServiceId: Long): StationServiceEntity {
        return stationServiceBox[stationServiceId]
    }

    fun getHistoryStationService(stationServiceId: Long): HistoryStationServiceEntity? {
        val result = historyStationServiceBox.query()
            .equal(HistoryStationServiceEntity_.stationServiceId, stationServiceId).build().find()
        if (result.size == 0)
            return null
        return result[0]
    }

    fun getAllHistoryStationService(): List<HistoryStationServiceEntity> =
        historyStationServiceBox.all

    fun getHistoryMinPrice(): Double {
        return historyStationServiceBox.query()
            //Descartamos canarias con id 5, ceuta 18, melilla 19, por que no hacen media
            .notEqual(HistoryStationServiceEntity_.idCCAA, 5)
            .notEqual(HistoryStationServiceEntity_.idCCAA, 18)
            .notEqual(HistoryStationServiceEntity_.idCCAA, 19)
            .build().property(HistoryStationServiceEntity_.price).minDouble()
    }

    fun getHistoryMaxPrice(): Double {
        return historyStationServiceBox.query().build().property(HistoryStationServiceEntity_.price)
            .maxDouble()
    }

    fun getHistoryInRangeByLatLongAndPriceByPoly(
        poly: List<LatLng>,
        maxPrice: Double,
        stationServiceNear: MutableLiveData<MutableList<HistoryStationServiceEntity>>,
        loadingPossibleStationService: MutableLiveData<Resource<Void>>
    ) {
        loadingPossibleStationService.value = Resource.loading()
        CoroutineScope(Dispatchers.IO).launch {
            historyStationServiceBox.query()
                .filter { stationService ->
                    stationService.price <= maxPrice
                            && GeoDistanceAlgorithm.bdccGeoDistanceCheckWithRadius(
                        poly,
                        LatLng(
                            stationService.stationService.target.latitude,
                            stationService.stationService.target.longitude
                        ),
                        DEFAULT_DISTANCE
                    )
                }.build().find().apply {
                    stationServiceNear.postValue(this)
                    loadingPossibleStationService.postValue(Resource.success(null, 200))
                }
        }
    }

    fun getStationServiceHistoryByStationServiceIdAndFuelTypeId(
        stationServiceId: Long, fuelTypeId: Long,
        result: MediatorLiveData<Resource<List<HistoryStationServiceModel>>>
    ) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    stationServiceService
                        .getStationServiceHistoryByStationServiceIdAndFuelTypeId(
                            stationServiceId = stationServiceId,
                            fuelTypeId = fuelTypeId
                        )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        result.value = Resource.success(response.body(), response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(
                            response.errorBody()?.string(),
                            object : TypeToken<ErrorModel>() {}.type
                        )
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }
}