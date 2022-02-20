package com.martinlacorrona.ryve.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivityHistoryBinding
import com.martinlacorrona.ryve.mobile.model.HistoryStationServiceModel
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@AndroidEntryPoint
class HistoryActivity : BaseActivity() {

    private val vm: HistoryViewModel by viewModels()
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)
        binding.lifecycleOwner = this
        binding.vm = vm

        initTopBar()
        loadExtras()
        initObservers()
        initChart()
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

    override fun finish() {
        if(vm.mode.value == HISTORY_ACTIVITY_FUEL_MODE_NOTIFICATION) {
            Intent(this, UpdateActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(it)
            }
        }
        else
            super.finish()
    }

    private fun initTopBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadExtras() {
        intent.extras?.let { bundle ->
            vm.stationServiceId.value = bundle.getLong(HISTORY_ACTIVITY_STATION_SERVICE_ID)
            if(bundle.getLong(HISTORY_ACTIVITY_FUEL_TYPE_ID) != 0L)
                vm.fuelTypeId.value = bundle.getLong(HISTORY_ACTIVITY_FUEL_TYPE_ID)
            else
                vm.fuelTypeId.value = vm.getUserFavouriteFuelId()
            vm.mode.value = bundle.getByte(HISTORY_ACTIVITY_FUEL_MODE, HISTORY_ACTIVITY_FUEL_MODE_DEFAULT)
        }
    }

    private fun initObservers() {
        vm.historyStationServices.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(getString(R.string.loading_history))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    addDataToChat(it.data)
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    if (it.code == -1)
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.search),
                                getString(R.string.error_connecting),
                                onPositive = {finish()},
                                positiveText = getString(R.string.ok)
                            )
                        )
                    else
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.search),
                                it.message,
                                onPositive = {finish()},
                                positiveText = getString(R.string.ok)
                            )
                        )
                }
            }
        }
    }

    private fun initChart() {
        binding.chart.apply {
            xAxis.granularity = 1F
            xAxis.textSize = 10F
            axisLeft.textSize = 18F
            axisRight.textSize = 18F
            description = Description().apply {
                text = ""
            }
            animateX(1000)
            setNoDataText(getString(R.string.no_data_available))
            marker = CustomMarker(this@HistoryActivity, R.layout.marker_view)
            invalidate() //refresh chat
        }
    }

    private fun addDataToChat(data: List<HistoryStationServiceModel>?) {
        //Usamos el hashmap para guardar el modelo a la hora de pintarlo
        val entriesHashMap = HashMap<Float, HistoryStationServiceModel>()
        val entries = ArrayList<Entry>()
        var num = 0F
        data?.forEach { stationServiceModel ->
            entries.add(Entry(num, stationServiceModel.price.toFloat()))
            entriesHashMap[num] = stationServiceModel
            num++
        }
        val dataSet = LineDataSet(entries, getString(R.string.price)).apply {
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = getColor(R.color.colorPrimaryDark)
        }
        binding.chart.apply {
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase): String {
                    val sd = SimpleDateFormat("dd/MM/yyyy")
                    return entriesHashMap[value]?.date?.let { sd.format(it) } ?: ""
                }
            }
            setData(LineData(dataSet))
            invalidate() //refresh chat
        }
    }

    inner class CustomMarker(context: Context, layoutResource: Int):  MarkerView(context, layoutResource) {
        override fun refreshContent(entry: Entry?, highlight: Highlight?) {
            val value = entry?.y?.toDouble() ?: 0.0
            var resText: String
            if(value.toString().length > 8){
                resText = "${value.toString().substring(0,5)} €"
            }
            else{
                resText = "$value €"
            }
            findViewById<TextView>(R.id.price).text = resText
            super.refreshContent(entry, highlight)
        }

        override fun getOffsetForDrawingAtPoint(xpos: Float, ypos: Float): MPPointF {
            return MPPointF(-width / 2f, -height - 10f)
        }
    }

    companion object {
        const val HISTORY_ACTIVITY_STATION_SERVICE_ID = "HISTORY_ACTIVITY_STATION_SERVICE_ID"
        const val HISTORY_ACTIVITY_FUEL_TYPE_ID = "HISTORY_ACTIVITY_FUEL_TYPE_ID"
        const val HISTORY_ACTIVITY_FUEL_MODE = "HISTORY_ACTIVITY_FUEL_MODE"

        const val HISTORY_ACTIVITY_FUEL_MODE_DEFAULT = 0.toByte()
        const val HISTORY_ACTIVITY_FUEL_MODE_NOTIFICATION = 1.toByte()


    }
}