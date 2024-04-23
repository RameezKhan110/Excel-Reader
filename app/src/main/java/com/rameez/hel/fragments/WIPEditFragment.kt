package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentWIPEditBinding
import com.rameez.hel.viewmodel.WIPViewModel

class WIPEditFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPEditBinding
    private val wipViewModel: WIPViewModel by activityViewModels()
    private var tagsList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWIPEditBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = arguments
        val id = intent?.getInt("wip_id")

        if (id != null) {
            wipViewModel.getWIPById(id)?.observe(viewLifecycleOwner) {
                mBinding.apply {
                    tvHeading.text = "Edit WIP"
                    etWord.setText(it.wip)
                    etMeaning.setText(it.meaning)
                    etSampleSentence.setText(it.sampleSentence)
                    etCategory.setText((it.category))
                    tvTags.text = it?.customTag?.joinToString(", ")
                    if (it.readCount != null) etRadCount.setText(it.readCount.toInt().toString())
                    if (it.displayCount != null) etViewedCount.setText(
                        it.displayCount!!.toInt().toString()
                    )
                    if (it?.customTag?.none() { it.isEmpty() } == true) {
                        tagsList = it.customTag as ArrayList<String>
                    }
                }
            }
        } else {
            mBinding.tvHeading.text = "Add WIP"
        }

        mBinding.apply {
            imgBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSave.setOnClickListener {
                val wip = etWord.text.toString().trim()
                val meaning = etMeaning.text.toString().trim()
                val sampleSentence = etSampleSentence.text.toString().trim()
                val tags = tvTags.text.split(",").map { it.trim() }
                val readCount = etRadCount.text.toString().toFloat()
                val viewCount = etViewedCount.text.toString().toFloat()
                val category = etCategory.text.toString()

                if (id != null) {
                    wipViewModel.updateWIP(
                        id,
                        category,
                        wip,
                        meaning,
                        sampleSentence,
                        tags,
                        readCount,
                        viewCount
                    )
                    findNavController().navigateUp()
                } else {

                    var sr: Float
                    wipViewModel.getWIPs()?.observe(viewLifecycleOwner) {
                        sr = it.size.toFloat() + 1.0f

                        val wipItem = WIPModel(
                            sr = sr,
                            category = category,
                            wip = wip,
                            meaning = meaning,
                            sampleSentence = sampleSentence,
                            customTag = tags,
                            readCount = readCount,
                            displayCount = viewCount
                        )
                        if(wip.isNotBlank() && category.isNotBlank()) {
                            wipViewModel.insertWIP(wipItem)
                            Toast.makeText(requireContext(), "New WIP added", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(requireContext(), "WIP and category can't be empty", Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            }

            btnAddTag.setOnClickListener {
                etTag.text?.toString()?.trim()?.let { it1 -> tagsList.add(it1) }
                Log.d("TAG", "tag list $tagsList")
                tvTags.text = tagsList.joinToString(", ")
                etTag.setText("")
            }

        }
    }

}