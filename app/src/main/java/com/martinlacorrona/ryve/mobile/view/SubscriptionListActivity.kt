package com.martinlacorrona.ryve.mobile.view

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.ActivitySubscriptionListBinding
import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.view.subscriptionlist.SubscriptionViewAdapter
import com.martinlacorrona.ryve.mobile.viewmodel.SubscriptionListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionListActivity : BaseActivity() {

    val TAG = "SuscriptionListActivity"

    private val vm: SubscriptionListViewModel by viewModels()
    private lateinit var binding: ActivitySubscriptionListBinding

    private lateinit var subscriptionList: Array<SubscribeNotificationEntity>
    private lateinit var adapter: SubscriptionViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription_list)
        binding.lifecycleOwner = this
        binding.vm = vm

        initTopBar()
        initObservables()
        initRecyclerView()
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

    private fun initObservables() {
        vm.removeSubscriptionResource.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(getString(R.string.removing_alert))
                }
                Resource.Status.SUCCESS -> {
                    closeLoading()
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.remove_alert),
                            getString(R.string.remove_succesfully)
                        )
                    )

                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    if (it.code == -1)
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.remove_alert),
                                getString(R.string.error_connecting)
                            )
                        )
                    else
                        setAlertRetry(
                            AlertRetry(
                                getString(R.string.remove_alert),
                                it.message
                            )
                        )
                }
            }
        }
    }

    private fun initRecyclerView() {
        vm.subscriptionList.observe(this) {
            it?.let {
                subscriptionList = it.toList().toTypedArray()
                vm.numberOfSuscriptions.value = subscriptionList.size
                adapter = SubscriptionViewAdapter(subscriptionList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.smoothScrollToPosition(0)
            }
        }
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                setAlertRetry(
                    AlertRetry(
                        getString(R.string.remove_alert),
                        getString(R.string.are_you_sure_delete_alert),
                        onPositive = {
                            vm.unsubscribe(subscriptionList[viewHolder.adapterPosition].id)
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
}