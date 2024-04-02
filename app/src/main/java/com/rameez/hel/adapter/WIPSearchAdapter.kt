package com.rameez.hel.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.WipListItemLayoutBinding
import com.rameez.hel.databinding.WipSearchItemLayoutBinding

class WIPSearchAdapter : ListAdapter<WIPModel, RecyclerView.ViewHolder>(WIPDiffUtil()) {

    var onWipItemClicked: ((Int) -> Unit) ? = null

    class WIPDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<WIPModel>() {
        override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }
    }

    inner class WIPListViewHolder(private val binding: WipSearchItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(wipItem: WIPModel) {
            binding.apply {

                txtWord.text = wipItem.wip
                txtMeaning.text = wipItem.meaning

                wipCv.setOnClickListener {
                    wipItem.id?.let { it1 -> onWipItemClicked?.invoke(it1) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WIPListViewHolder(
            WipSearchItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as WIPListViewHolder).bind(item)
    }
}