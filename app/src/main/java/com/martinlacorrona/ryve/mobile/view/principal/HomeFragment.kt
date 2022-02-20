package com.martinlacorrona.ryve.mobile.view.principal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.navigation.navGraphViewModels
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.FragmentHomeBinding
import com.martinlacorrona.ryve.mobile.model.FilterModel
import com.martinlacorrona.ryve.mobile.view.BaseFragment
import com.martinlacorrona.ryve.mobile.view.FilterActivity
import com.martinlacorrona.ryve.mobile.view.FilterActivity.Companion.FILTER_ACTIVITY_FILTER_MODEL
import com.martinlacorrona.ryve.mobile.view.FilterActivity.Companion.FILTER_ACTIVITY_RESULT
import com.martinlacorrona.ryve.mobile.view.principal.home.StationServiceHomeViewAdapter
import com.martinlacorrona.ryve.mobile.viewmodel.principal.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment @Inject constructor() : BaseFragment() {

    private val vm: HomeViewModel
        by navGraphViewModels(R.id.mobile_navigation) {
            defaultViewModelProviderFactory
        } //Use for save viewmodel state in mobile_navigation
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.title = getString(R.string.menu_home)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.vm = vm

        setHasOptionsMenu(true)

        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView() {
        val adapter = StationServiceHomeViewAdapter()
        vm.stationServiceList.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(null) //Send it null to go top again
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
                activity?.title = "${getString(R.string.menu_home)} (${it.count()})"
                binding.recyclerView.smoothScrollToPosition(0)
            }
        }
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_options, menu)

        val searchItem = menu.findItem(R.id.home_options_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it == "") {
                        vm.resetQueryByName()
                    } else {
                        vm.queryByName(it)
                    }
                    binding.recyclerView.smoothScrollToPosition(0)
                }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.home_options_filter -> {
            loadFilterActivity()
            true
        }

        else -> {
            false
        }
    }

    private fun loadFilterActivity() {
        Intent(context, FilterActivity::class.java).apply {
            putExtra(FILTER_ACTIVITY_FILTER_MODEL, vm.filterModel)
            startActivityForResult(this, FILTER_ACTIVITY_RESULT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILTER_ACTIVITY_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.apply {
                    (get(FILTER_ACTIVITY_FILTER_MODEL) as FilterModel).apply {
                        vm.filterModel.orderBy = orderBy
                        vm.filterModel.orderWay = orderWay
                        vm.filterModel.priceMin = priceMin
                        vm.filterModel.priceMax = priceMax
                        vm.filterModel.ccaa = ccaa
                        vm.filterModel.ccaaId = ccaaId
                    }
                    vm.queryByFilterAgain()
                }
            }
        }
    }
}