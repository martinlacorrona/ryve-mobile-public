package com.martinlacorrona.ryve.mobile.view.principal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.FragmentFavouritesBinding
import com.martinlacorrona.ryve.mobile.entity.FavouriteStationServiceEntity
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.BaseActivity
import com.martinlacorrona.ryve.mobile.view.BaseFragment
import com.martinlacorrona.ryve.mobile.view.principal.favourite.StationServiceFavouriteViewAdapter
import com.martinlacorrona.ryve.mobile.viewmodel.principal.FavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavouritesFragment : BaseFragment() {

    private val vm: FavouritesViewModel by viewModels()
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favouritesList : Array<FavouriteStationServiceEntity>
    private lateinit var adapter : StationServiceFavouriteViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.title = getString(R.string.menu_favourites)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourites, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.vm = vm

        initRecyclerView()
        initObservers()

        return binding.root
    }

    private fun initRecyclerView() {
        vm.stationServiceList.observe(viewLifecycleOwner) {
            it?.let {
                favouritesList = it.toList().toTypedArray()
                vm.numberOfFavourite.value = favouritesList.size
                adapter = StationServiceFavouriteViewAdapter(favouritesList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.smoothScrollToPosition(0)
            }
        }
        binding.recyclerView.isNestedScrollingEnabled = false
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.remove_favourites),
                        getString(R.string.are_you_sure_delete_favourite),
                        onPositive = {
                            vm.deleteFavourite(favouritesList[viewHolder.adapterPosition].id)
                            adapter.notifyItemChanged(viewHolder.adapterPosition)
                        },
                        positiveText = getString(R.string.ok),
                        onNegative = {
                            adapter.notifyItemChanged(viewHolder.adapterPosition)
                        },
                        negativeText = getString(R.string.cancel)
                    )
                )
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        vm.removeFavouriteResource.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    (activity as BaseActivity).setLoading(getString(R.string.remove_to_favourites))
                }
                Resource.Status.SUCCESS -> {
                    (activity as BaseActivity).closeLoading()
                    if (it.code != 200) {
                        (activity as BaseActivity).setAlertRetry(
                            BaseActivity.AlertRetry(
                                getString(R.string.remove_favourites),
                                getString(R.string.error_ocurred_removing_favourite)
                            )
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    (activity as BaseActivity).closeLoading()
                    (activity as BaseActivity).setAlertRetry(
                        BaseActivity.AlertRetry(
                            getString(R.string.remove_favourites),
                            getString(R.string.error_connecting)
                        )
                    )
                }
            }
        }
    }
}