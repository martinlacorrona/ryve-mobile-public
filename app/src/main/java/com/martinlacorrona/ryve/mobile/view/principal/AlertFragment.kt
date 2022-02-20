package com.martinlacorrona.ryve.mobile.view.principal

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.databinding.FragmentAlertBinding
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity
import com.martinlacorrona.ryve.mobile.view.BaseActivity
import com.martinlacorrona.ryve.mobile.view.BaseFragment
import com.martinlacorrona.ryve.mobile.view.HistoryActivity
import com.martinlacorrona.ryve.mobile.view.SubscriptionListActivity
import com.martinlacorrona.ryve.mobile.view.principal.alert.NotificationViewAdapter
import com.martinlacorrona.ryve.mobile.viewmodel.principal.AlertViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlertFragment @Inject constructor() : BaseFragment() {

    private val vm: AlertViewModel by viewModels()
    private var _binding: FragmentAlertBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationList: Array<NotificationEntity>
    private lateinit var adapter: NotificationViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.title = getString(R.string.menu_alert)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.vm = vm

        setHasOptionsMenu(true)

        initRecyclerView()
        initBindings()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alert_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.alert_options_mark_all_as_read -> {
            (activity as BaseActivity).setAlertRetry(
                BaseActivity.AlertRetry(
                    getString(R.string.mark_all_as_readed),
                    getString(R.string.are_you_sure_mark_all_as_read),
                    onPositive = {
                        vm.markAllNotificationAsRead()
                        adapter.notifyDataSetChanged()
                    },
                    positiveText = getString(R.string.ok),
                    onNegative = {},
                    negativeText = getString(R.string.cancel)
                )
            )
            true
        }

        else -> {
            false
        }
    }

    private fun initRecyclerView() {
        vm.notificationList.observe(viewLifecycleOwner) {
            it?.let {
                notificationList = it.toList().toTypedArray()
                vm.numberOfNotifications.value = notificationList.size
                adapter = NotificationViewAdapter(notificationList, vm)
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
                (activity as BaseActivity).setAlertRetry(
                    BaseActivity.AlertRetry(
                        getString(R.string.remove_notification),
                        getString(R.string.are_you_sure_delete_notification),
                        onPositive = {
                            vm.deleteNotification(notificationList[viewHolder.adapterPosition].id)
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

    private fun initBindings() {
        binding.buttonShowSubscriptionList.setOnClickListener {
            Intent(context, SubscriptionListActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}