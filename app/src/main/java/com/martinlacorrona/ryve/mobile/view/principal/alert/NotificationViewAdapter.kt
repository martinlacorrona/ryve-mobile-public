package com.martinlacorrona.ryve.mobile.view.principal.alert

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.databinding.NotificationAlertViewBinding
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity
import com.martinlacorrona.ryve.mobile.view.DetailActivity
import com.martinlacorrona.ryve.mobile.viewmodel.principal.AlertViewModel

class NotificationViewAdapter(private val dataSet: Array<NotificationEntity>,
private val vm: AlertViewModel) :
    RecyclerView.Adapter<NotificationViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotificationAlertViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    inner class ViewHolder(val binding: NotificationAlertViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationEntity) {
            binding.item = item
            binding.mainLayout.setOnClickListener {
                vm.markNotificationAsRead(item.id) //mark notification as read
                Intent(binding.root.context, DetailActivity::class.java).apply {
                    putExtra(
                        DetailActivity.DETAIL_ACTIVITY_STATION_SERVICE_ID,
                        item.stationServiceId
                    )
                    binding.root.context.startActivity(this)
                }
            }
        }
    }
}