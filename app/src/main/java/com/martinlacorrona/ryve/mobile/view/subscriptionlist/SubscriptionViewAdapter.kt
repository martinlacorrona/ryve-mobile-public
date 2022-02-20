package com.martinlacorrona.ryve.mobile.view.subscriptionlist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.databinding.SuscriptionNotificationSuscriptionListViewBinding
import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import com.martinlacorrona.ryve.mobile.view.DetailActivity

class SubscriptionViewAdapter(
    private val dataSet: Array<SubscribeNotificationEntity>
) :
    RecyclerView.Adapter<SubscriptionViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
            SuscriptionNotificationSuscriptionListViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    inner class ViewHolder(val binding: SuscriptionNotificationSuscriptionListViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubscribeNotificationEntity) {
            binding.item = item
            binding.mainLayout.setOnClickListener {
                Intent(binding.root.context, DetailActivity::class.java).apply {
                    putExtra(
                        DetailActivity.DETAIL_ACTIVITY_STATION_SERVICE_ID,
                        item.stationService.target.id
                    )
                    binding.root.context.startActivity(this)
                }
            }
        }
    }
}