package com.example.airpollutionmonitor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.airpollutionmonitor.R
import com.example.airpollutionmonitor.data.Record
import com.example.airpollutionmonitor.databinding.HighPollutionListItemBinding
import com.example.airpollutionmonitor.utils.GlideUtils

private const val TAG = "HighPollutedAdapter"

class HighPollutedAdapter : RecyclerView.Adapter<HighPollutedAdapter.ViewHolder>(), Filterable {
    var fullData = listOf<Record>()
    var filterData = listOf<Record>()

    class ViewHolder(private val binding: HighPollutionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int, record: Record) {
            val isStateFine =
                record.status == binding.root.context.resources.getText(R.string.good_status)

            binding.numberTextView.text = (pos + 1).toString()
            binding.siteTextView.text = record.sitename
            binding.countyTextView.text = record.county
            binding.pm25ValueTextView.text = record.pm25
            binding.statusTextView.text =
                if (isStateFine)
                    binding.root.context.resources.getText(R.string.good_status_hint)
                else
                    record.status
            binding.imgBtn.apply {
                visibility = if (isStateFine) View.GONE else View.VISIBLE
                setOnClickListener {
                    Toast.makeText(
                        context,
                        binding.root.context.resources.getText(R.string.normal_status_toast_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            GlideUtils.loadIconByCountry(binding.root.context, binding.iconImageView, record.county)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HighPollutionListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, filterData[position])
    }

    override fun getItemCount(): Int = filterData.size

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(str: CharSequence): FilterResults {
            val filteredList: ArrayList<Record> = ArrayList()
            if (str.isEmpty()) {
                filteredList.addAll(fullData)
            } else {
                fullData.filterTo(filteredList) {
                    it.county.contains(str, true)
                            || it.sitename.contains(str, true)
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filterData = results.values as ArrayList<Record>
            notifyDataSetChanged()
        }
    }

}
