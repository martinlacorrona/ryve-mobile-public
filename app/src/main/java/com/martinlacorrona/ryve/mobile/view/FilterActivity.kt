package com.martinlacorrona.ryve.mobile.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityFilterBinding
import com.martinlacorrona.ryve.mobile.model.FilterModel
import com.martinlacorrona.ryve.mobile.viewmodel.FilterViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FilterActivity : BaseActivity() {

    val TAG = "FilterActivity"

    private val vm: FilterViewModel by viewModels()
    private lateinit var binding: ActivityFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter)
        binding.lifecycleOwner = this
        binding.vm = vm

        initTopBar()
        loadExtras()
        initCombos()
        initBinding()
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
        intent.extras?.apply {
            (get(FILTER_ACTIVITY_FILTER_MODEL) as FilterModel).apply {
                vm.orderBy.value = orderBy
                vm.orderWay.value = orderWay
                priceMin?.let { vm.minPrice.value = it.toString() }
                priceMax?.let { vm.maxPrice.value = it.toString() }
                vm.ccaa.value = ccaa
                vm.ccaaId.value = ccaaId
            }
        }
    }

    private fun initCombos() {
        val adapterOrderBy = ArrayAdapter(
            applicationContext, R.layout.list_item, arrayListOf(
                OrderByWithName(FilterViewModel.OrderBy.NAME, getString(R.string.name)),
                OrderByWithName(FilterViewModel.OrderBy.PRICE, getString(R.string.price))
            )
        )
        binding.comboFieldOrderByList.apply {
            vm.orderBy.value?.let {
                setText(
                    if (it == FilterViewModel.OrderBy.NAME) getString(R.string.name) else getString(
                        R.string.price
                    )
                )
            }
            setAdapter(adapterOrderBy)
            setOnItemClickListener { _, _, position, _ ->
                vm.orderBy.value = adapterOrderBy.getItem(position)?.orderBy
            }
        }
        val adapterOrderWay = ArrayAdapter(
            applicationContext, R.layout.list_item, arrayListOf(
                OrderWayWithName(FilterViewModel.OrderWay.ASC, getString(R.string.ascendent)),
                OrderWayWithName(FilterViewModel.OrderWay.DESC, getString(R.string.descendent))
            )
        )
        binding.comboFieldOrderWayList.apply {
            vm.orderWay.value?.let {
                setText(
                    if (it == FilterViewModel.OrderWay.ASC) getString(R.string.ascendent) else getString(
                        R.string.descendent
                    )
                )
            }
            setAdapter(adapterOrderWay)
            setOnItemClickListener { _, _, position, _ ->
                vm.orderWay.value = adapterOrderWay.getItem(position)?.orderWay
            }
        }
        val adapterCCAA = ArrayAdapter(
            applicationContext, R.layout.list_item, arrayListOf(
                CCAA(0, getString(R.string.all_comunities)),
                CCAA(1, "Andalucia"),
                CCAA(2, "Aragón"),
                CCAA(3, "Asturias"),
                CCAA(4, "Baleares"),
                CCAA(5, "Canarias"),
                CCAA(6, "Cantabria"),
                CCAA(7, "Castilla la Mancha"),
                CCAA(8, "Castilla y León"),
                CCAA(9, "Cataluña"),
                CCAA(10, "Comunidad Valenciana"),
                CCAA(11, "Extremadura"),
                CCAA(12, "Galicia"),
                CCAA(13, "Madrid"),
                CCAA(14, "Murcia"),
                CCAA(15, "Navarra"),
                CCAA(16, "País Vasco"),
                CCAA(17, "La Rioja"),
                CCAA(18, "Ceuta"),
                CCAA(19, "Melilla"),
            )
        )
        binding.comboFieldCcaaList.apply {
            vm.ccaaId.value?.let {
                vm.ccaa.value?.let {
                    setText(
                        vm.ccaa.value
                    )
                }
            }
            setAdapter(adapterCCAA)
            setOnItemClickListener { _, _, position, _ ->
                vm.ccaaId.value = adapterCCAA.getItem(position)?.id
                vm.ccaa.value = adapterCCAA.getItem(position)?.name
            }
        }
    }

    private fun initBinding() {
        binding.buttonApply.setOnClickListener {
            val data = Intent().apply {
                putExtra(
                    FILTER_ACTIVITY_FILTER_MODEL, FilterModel(
                        orderBy = vm.orderBy.value ?: FilterViewModel.OrderBy.PRICE,
                        orderWay = vm.orderWay.value ?: FilterViewModel.OrderWay.ASC,
                        priceMin = convertToDouble(vm.minPrice.value),
                        priceMax = convertToDouble(vm.maxPrice.value),
                        ccaa = vm.ccaa.value,
                        ccaaId = vm.ccaaId.value
                    )
                )
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun convertToDouble(string: String?): Double? {
        if (string == null || string == "")
            return null
        return string.toDouble()
    }

    data class OrderByWithName(
        val orderBy: FilterViewModel.OrderBy,
        val name: String
    ) {
        override fun toString() = name
    }

    data class OrderWayWithName(
        val orderWay: FilterViewModel.OrderWay,
        val name: String
    ) {
        override fun toString() = name
    }

    data class CCAA(
        val id: Long,
        val name: String
    ) {
        override fun toString() = name
    }

    companion object {
        const val FILTER_ACTIVITY_RESULT = 1
        const val FILTER_ACTIVITY_FILTER_MODEL = "FILTER_ACTIVITY_FILTER_MODEL"
    }
}