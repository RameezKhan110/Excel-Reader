package com.rameez.hel.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.CarouselItemBinding


class CarouselAdapter :
    ListAdapter<WIPModel, RecyclerView.ViewHolder>(CarouselDiffUtil()) {

    var onItemClick: ((Int) -> Unit)? = null
    class CarouselDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<WIPModel>() {
        override fun areItemsTheSame(oldItem: WIPModel, newItem: WIPModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: WIPModel,
            newItem: WIPModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class CarouselViewHolder(private val binding: CarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(item: WIPModel) {
                binding.apply {
                    txtWordType.text = item.category
                    txtWord.text = item.wip
                    txtWordMeaning.text = item.meaning
                    txtViewCount.text = "Viewed " + item.displayCount?.toInt().toString() + " times"
                    txtReadCount.text = "Read " + item.readCount?.toInt().toString() + " times"
                    txtSampleSentence.text = item.sampleSentence
                    txtTags.text = item.customTag?.joinToString(", ")

                    wipCv.setOnClickListener {
                        item.id?.let { it1 -> onItemClick?.invoke(it1) }
                    }
                }
            }
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as CarouselViewHolder).bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CarouselViewHolder(
            CarouselItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

}