package com.rameez.hel.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.WipListItemLayoutBinding

class WIPListAdapter : ListAdapter<WIPModel, RecyclerView.ViewHolder>(WIPDiffUtil()) {

    var onWipItemClicked: ((Int, Float, Int) -> Unit) ? = null
    class WIPDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<WIPModel>() {
        override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }
    }

    inner class WIPListViewHolder(private val binding: WipListItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(wipItem: WIPModel) {
                binding.apply {
                    txtWordType.text = wipItem?.category
                    txtWord.text = wipItem?.wip
                    txtWordMeaning.text = wipItem?.meaning
                    if(wipItem.displayCount == null) {
                        txtCount.text = "Viewed 0 times"
                    } else {
                        txtCount.text = "Viewed " + wipItem.displayCount!!.toInt().toString() + " times"
                    }

                    if(wipItem.readCount == null) {
                        txtEncountered.text = "Encountered 0 times"
                    } else {
                        txtEncountered.text = "Encountered " + wipItem.readCount!!.toInt().toString() + " times"
                    }


                    wipCv.setOnClickListener {
                        wipItem.id?.let { it1 -> wipItem.displayCount?.let { it2 ->
                            onWipItemClicked?.invoke(it1,
                                it2,
                                position
                            )
                        } }
                    }

//                    val incCount = wipItem.displayCount?.toInt()?.plus(1)
//                    wipItem.id?.let { incCount?.toFloat()
//                        ?.let { it1 -> onIncViewedCount?.invoke(it, it1) } }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WIPListViewHolder(
            WipListItemLayoutBinding.inflate(
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