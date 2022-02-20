package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
) : BaseViewModel() {

    val TAG = "FilterViewModel"

    val orderBy = MutableLiveData<OrderBy>()
    val orderWay = MutableLiveData<OrderWay>()
    val ccaa = MutableLiveData<String>()
    val ccaaId = MutableLiveData<Long>()
    val minPrice = MutableLiveData<String>()
    val maxPrice = MutableLiveData<String>()

    enum class OrderBy {
        NAME, PRICE
    }

    enum class OrderWay {
        ASC, DESC
    }
}