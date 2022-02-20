package com.martinlacorrona.ryve.mobile.view.principal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.FragmentTravelBinding
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.BaseActivity
import com.martinlacorrona.ryve.mobile.view.BaseFragment
import com.martinlacorrona.ryve.mobile.view.RouteActivity
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_AVOID_TOLLS
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_DESTINATION
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_DISTANCE_IN_METERS
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_DURATION_IN_MINUTES
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_MAX_DISTANCE
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_MAX_PRICE
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_NORTHWEST_BOUND
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_ORIGIN
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_PERCENTAGE
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_POINTS_ENCODED
import com.martinlacorrona.ryve.mobile.view.RouteActivity.Companion.ROUTE_ACTIVITY_SOUTHWEST_BOUND
import com.martinlacorrona.ryve.mobile.view.util.InputFilterMinMax
import com.martinlacorrona.ryve.mobile.viewmodel.principal.TravelViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TravelFragment @Inject constructor() : BaseFragment() {

    val TAG = "TravelFragment"

    private val vm: TravelViewModel by viewModels()
    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!

    private var selectedInSearch = ORIGIN_SELECTED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.title = getString(R.string.menu_travel)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_travel, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.vm = vm

        initCombos()
        initBindings()
        initObservable()

        return binding.root
    }

    private fun initCombos() {
        val adapterAvoidTolls = ArrayAdapter(
            requireContext(), R.layout.list_item, arrayListOf(
                YesOrNoWithName(true, getString(R.string.yes)),
                YesOrNoWithName(false, getString(R.string.no))
            )
        )
        binding.comboFieldAvoidTollsList.apply {
            vm.avoidTolls.value?.let {
                setText(if (it) getString(R.string.yes) else getString(R.string.no))
            }
            setAdapter(adapterAvoidTolls)
            setOnItemClickListener { _, _, position, _ ->
                vm.avoidTolls.value = adapterAvoidTolls.getItem(position)?.value
            }
        }
    }

    data class YesOrNoWithName(
        val value: Boolean,
        val name: String
    ) {
        override fun toString() = name
    }

    private fun initBindings() {
        binding.textFieldEditTextOriginText.setOnClickListener {
            selectedInSearch = ORIGIN_SELECTED
            loadAutoCompleteIntent()
        }
        binding.textFieldEditTextDestinationText.setOnClickListener {
            selectedInSearch = DESTINATION_SELECTED
            loadAutoCompleteIntent()
        }
        binding.textFieldEditTextPercentageLoadedText.filters =
            arrayOf<InputFilter>(InputFilterMinMax(0.1, 100.0))
        binding.textFieldEditTextMaxDistanceText.filters =
            arrayOf<InputFilter>(InputFilterMinMax(0.1, 2500.0))
        binding.textFieldEditTextPriceMaxText.filters =
            arrayOf<InputFilter>(InputFilterMinMax(0.0, 100.0))
        binding.buttonTravel.setOnClickListener {
            openTravelIntent()
        }
    }

    private fun initObservable() {
        vm.directionsResource.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    (activity as BaseActivity).setLoading(getString(R.string.generating_route_process))
                }
                Resource.Status.SUCCESS -> {
                    (activity as BaseActivity).closeLoading()
                    Intent(context, RouteActivity::class.java).also {intent ->
                        Log.d("tatin", it.data.toString())
                        it.data?.let { data ->
                            intent.putExtra(ROUTE_ACTIVITY_ORIGIN, vm.originLatLng.value)
                            intent.putExtra(ROUTE_ACTIVITY_DESTINATION, vm.destinationLatLng.value)
                            intent.putExtra(ROUTE_ACTIVITY_AVOID_TOLLS, vm.avoidTolls.value)

                            intent.putExtra(ROUTE_ACTIVITY_MAX_PRICE, vm.maxPrice.value?.toDouble())
                            intent.putExtra(ROUTE_ACTIVITY_MAX_DISTANCE, vm.maxDistance.value?.toDouble())
                            intent.putExtra(ROUTE_ACTIVITY_PERCENTAGE, vm.percentageLoaded.value?.toDouble())

                            intent.putExtra(ROUTE_ACTIVITY_POINTS_ENCODED, data.pointsEncoded)
                            intent.putExtra(ROUTE_ACTIVITY_NORTHWEST_BOUND, data.northeast)
                            intent.putExtra(ROUTE_ACTIVITY_SOUTHWEST_BOUND, data.southwest)
                            intent.putExtra(ROUTE_ACTIVITY_DISTANCE_IN_METERS, data.distance)
                            intent.putExtra(ROUTE_ACTIVITY_DURATION_IN_MINUTES, data.duration)
                            startActivity(intent)
                        } ?: run{
                            (activity as BaseActivity).setAlertRetry(
                                BaseActivity.AlertRetry(
                                    getString(R.string.generating_route),
                                    getString(R.string.error_routing_from_server)
                                )
                            )
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    (activity as BaseActivity).closeLoading()
                    if (it.code == -1)
                        (activity as BaseActivity).setAlertRetry(
                            BaseActivity.AlertRetry(
                                getString(R.string.generating_route),
                                getString(R.string.error_connecting)
                            )
                        )
                    else
                        (activity as BaseActivity).setAlertRetry(
                            BaseActivity.AlertRetry(
                                getString(R.string.generating_route),
                                it.message
                            )
                        )
                }
            }
        }
    }

    private fun openTravelIntent() {
        if (validate()) {
            vm.loadDirections()
        }
    }

    private fun validate(): Boolean {
        when {
            vm.originLatLng.value == null || vm.destinationLatLng.value == null -> {
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.error_preparing_trip),
                        getString(R.string.select_origin_and_destination),
                    )
                )
                return false
            }
            vm.percentageLoaded.value!!.toDouble() < 20.0 -> {
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.error_preparing_trip),
                        getString(R.string.least_20_loaded_percentaje),
                    )
                )
                return false
            }
            vm.maxDistance.value!!.toDouble() < 50.0 -> {
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.error_preparing_trip),
                        getString(R.string.max_distance_at_least_50_km),
                    )
                )
                return false
            }
            vm.maxPrice.value!!.toDouble() <= 0.0 -> {
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.error_preparing_trip),
                        getString(R.string.max_price_more_than_zero),
                    )
                )
                return false
            }
        }
        return true
    }

    private fun loadAutoCompleteIntent() {
        //Datos que necesito
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        //Autocomplete de tipo overlay
        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(mutableListOf("ES")) //solo españa
            .build(requireContext()).apply {
                startActivityForResult(this, MapFragment.AUTOCOMPLETE_REQUEST_CODE)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MapFragment.AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        place.latLng?.let {
                            if (selectedInSearch == ORIGIN_SELECTED) {
                                vm.origin.value = place.name
                                vm.originLatLng.value = place.latLng
                            } else if (selectedInSearch == DESTINATION_SELECTED) {
                                vm.destination.value = place.name
                                vm.destinationLatLng.value = place.latLng
                            }
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 41

        const val ORIGIN_SELECTED = 1
        const val DESTINATION_SELECTED = 41
    }
}