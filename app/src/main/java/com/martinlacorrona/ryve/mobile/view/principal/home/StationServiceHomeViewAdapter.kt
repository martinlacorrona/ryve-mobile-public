package com.martinlacorrona.ryve.mobile.view.principal.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.databinding.StationServiceHomeViewBinding
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.view.DetailActivity
import com.martinlacorrona.ryve.mobile.view.FilterActivity

class StationServiceHomeViewAdapter : PagedListAdapter<HistoryStationServiceEntity, StationServiceHomeViewAdapter.ViewHolder>(
    DIFF_CALLBACK
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StationServiceHomeViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class ViewHolder(val binding: StationServiceHomeViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryStationServiceEntity) {
            binding.item = item
            binding.mainLayout.setOnClickListener {
                Intent(binding.root.context, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.DETAIL_ACTIVITY_STATION_SERVICE_ID, item.stationService.target.id)
                    binding.root.context.startActivity(this)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<HistoryStationServiceEntity>() {
            override fun areItemsTheSame(old: HistoryStationServiceEntity,
                                         new: HistoryStationServiceEntity) = old.id == new.id

            override fun areContentsTheSame(old: HistoryStationServiceEntity,
                                            new: HistoryStationServiceEntity) = old.id == new.id
        }
    }
}