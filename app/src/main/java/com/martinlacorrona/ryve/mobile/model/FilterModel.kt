package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.viewmodel.FilterViewModel
import java.io.Serializable

data class FilterModel(
    var orderBy: FilterViewModel.OrderBy = FilterViewModel.OrderBy.PRICE,
    var orderWay: FilterViewModel.OrderWay = FilterViewModel.OrderWay.ASC,
    var priceMin: Double? = null,
    var priceMax: Double? = null,
    var ccaa: String? = null,
    var ccaaId: Long? = 0L,
) : Serializable