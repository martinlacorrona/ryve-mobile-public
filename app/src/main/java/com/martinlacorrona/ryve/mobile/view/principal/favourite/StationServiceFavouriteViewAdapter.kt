package com.martinlacorrona.ryve.mobile.view.principal.favourite

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinlacorrona.ryve.mobile.databinding.StationServiceFavouriteViewBinding
import com.martinlacorrona.ryve.mobile.entity.FavouriteStationServiceEntity
import com.martinlacorrona.ryve.mobile.view.DetailActivity
import com.martinlacorrona.ryve.mobile.viewmodel.principal.FavouritesViewModel

class StationServiceFavouriteViewAdapter(private val dataSet: Array<FavouriteStationServiceEntity>) :
    RecyclerView.Adapter<StationServiceFavouriteViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StationServiceFavouriteViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    inner class ViewHolder(val binding: StationServiceFavouriteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavouriteStationServiceEntity) {
            binding.item = item
            binding.mainLayout.setOnClickListener {
                Intent(binding.root.context, DetailActivity::class.java).apply {
                    putExtra(
                        DetailActivity.DETAIL_ACTIVITY_STATION_SERVICE_ID,
                        item.id
                    )
                    binding.root.context.startActivity(this)
                }
            }
        }
    }
}