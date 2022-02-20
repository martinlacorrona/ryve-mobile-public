package com.martinlacorrona.ryve.mobile.view.principal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.FragmentMapBinding
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.view.BaseFragment
import com.martinlacorrona.ryve.mobile.view.DetailActivity
import com.martinlacorrona.ryve.mobile.viewmodel.principal.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.streams.toList


@AndroidEntryPoint
class MapFragment @Inject constructor() : BaseFragment(), OnMapReadyCallback {

    val TAG = "MapFragment"

    private val vm: MapViewModel by viewModels()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //Maps
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<HistoryStationServiceItem>

    //Localizacion y permisos
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        //DEFINIMOS EL CALLBACK DE LOS PERMISOS
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                actionsIfHavePermissionsEnabled()
            }
        }

        //Cliente de location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        activity?.title = getString(R.string.menu_map)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.vm = vm

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.map_options_search -> {
            loadAutoCompleteIntent()
            true
        }

        else -> {
            false
        }
    }

    private fun loadAutoCompleteIntent() {
        //Datos que necesito
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        //Autocomplete de tipo overlay
        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(mutableListOf("ES")) //solo españa
            .build(requireContext()).apply {
                startActivityForResult(this, AUTOCOMPLETE_REQUEST_CODE)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        firebaseAnalytics.logEvent("place_searched", Bundle().apply {
                            putString("latLng", place.latLng.toString())
                            putString("place_name", place.name)
                        })
                        place.latLng?.let {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 13F))
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //Ponemos el centro
        val center = LatLng(40.4637, -3.0)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 5F))
        ifHavePermissionsEnablePositionButtonAndMoveToThere()

        //Ponemos el clusterManager
        clusterManager = ClusterManager(context, map)
        clusterManager.apply {
            //OPTIMIZACION PARA QUE CARGUE MAS RAPIDO LOS CLUSTERS
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
            this.setAlgorithm(
                NonHierarchicalViewBasedAlgorithm<HistoryStationServiceItem>(
                    metrics.widthPixels, metrics.heightPixels
                )
            )
        }

        clusterManager.renderer = ClusterHistoryStationServiceRendered(context, map, clusterManager)
        clusterManager.setOnClusterItemInfoWindowClickListener {
            Intent(binding.root.context, DetailActivity::class.java).apply {
                putExtra(
                    DetailActivity.DETAIL_ACTIVITY_STATION_SERVICE_ID,
                    it.historyStationServiceEntity.stationService.target.id
                )
                binding.root.context.startActivity(this)
            }
        }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        //Añadimos las estaciones
        addItemsToListWithIcon()
    }

    /**
     * Añadimos las estaciones al cluster con su icono
     */
    private fun addItemsToListWithIcon() {
        //Calcular rango caro, medio y caro para asi poner los iconos
        val minPrice = vm.getHistoryMinPrice()
        val maxPrice = vm.getHistoryMaxPrice()
        val difference = maxPrice - minPrice
        val differenceDividedIn5 = difference / 5.0
        val superExpensiveRangeMin = maxPrice - differenceDividedIn5
        val expensiveRangeMin = maxPrice - differenceDividedIn5 * 2
        val mediumRangeMin = maxPrice - differenceDividedIn5 * 3
        val lowRangeMin = maxPrice - differenceDividedIn5 * 4

        clusterManager.addItems(vm.historyStationServiceList.parallelStream().map {
            when {
                it.price > superExpensiveRangeMin -> {
                    HistoryStationServiceItem(
                        it,
                        BitmapDescriptorFactory.fromResource(R.drawable.super_high_price)
                    )
                }
                it.price > expensiveRangeMin -> {
                    HistoryStationServiceItem(
                        it,
                        BitmapDescriptorFactory.fromResource(R.drawable.high_price)
                    )
                }
                it.price > mediumRangeMin -> {
                    HistoryStationServiceItem(
                        it,
                        BitmapDescriptorFactory.fromResource(R.drawable.medium_price)
                    )
                }
                it.price > lowRangeMin -> {
                    HistoryStationServiceItem(
                        it,
                        BitmapDescriptorFactory.fromResource(R.drawable.low_price)
                    )
                }
                else -> {
                    HistoryStationServiceItem(
                        it,
                        BitmapDescriptorFactory.fromResource(R.drawable.super_low_price)
                    )
                }
            }
        }.toList())
    }

    /**
     * Usamos esta funcion para ver si tenemos los permisos, si es asi, le damos el boton de
     * myLocation.
     */
    private fun ifHavePermissionsEnablePositionButtonAndMoveToThere() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //REQUEST PERMISSIONS
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return
        }
        actionsIfHavePermissionsEnabled()
    }

    /**
     * Acciones si tiene los permisos activados.
     * HAcemos el supress ya que ya hemos comprobado si los tiene
     */
    @SuppressLint("MissingPermission")
    private fun actionsIfHavePermissionsEnabled() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { //Como puede ser null por si acaso...
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 12F
                        )
                    )
                }
            }
    }

    /**
     * Clase interna para definir el ClusterItem para que se puedan agrupar
     */
    inner class HistoryStationServiceItem(
        var historyStationServiceEntity: HistoryStationServiceEntity,
        var icon: BitmapDescriptor
    ) : ClusterItem {
        override fun getPosition() = LatLng(
            historyStationServiceEntity.stationService.target.latitude,
            historyStationServiceEntity.stationService.target.longitude
        )

        override fun getTitle() = historyStationServiceEntity.stationService.target.name

        override fun getSnippet() =
            "${getString(R.string.price)}: ${historyStationServiceEntity.price} €"

    }

    inner class ClusterHistoryStationServiceRendered(
        context: Context?,
        map: GoogleMap?,
        clusterManager: ClusterManager<HistoryStationServiceItem>
    ) :
        DefaultClusterRenderer<HistoryStationServiceItem>(context, map, clusterManager) {
        override fun onBeforeClusterItemRendered(
            item: HistoryStationServiceItem,
            markerOptions: MarkerOptions
        ) {
            markerOptions
                .icon(item.icon)
                .title(item.title)
                .snippet(item.snippet)
        }

        override fun onClusterItemUpdated(item: HistoryStationServiceItem, marker: Marker) {
            marker
                .setIcon(item.icon)
        }
    }

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 51
    }
}