package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rameez.hel.R
import com.rameez.hel.SharedPref
import com.rameez.hel.databinding.FragmentWIPDetailBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


class WIPDetailFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPDetailBinding
    private val wipViewModel: WIPViewModel by activityViewModels()
    private lateinit var textToSpeech:  TextToSpeech
    private var word: String = ""
    private var id: Int = 0
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWIPDetailBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = arguments
        id = intent?.getInt("wip_id", 0) ?: 0
        wipViewModel.getWIPById(id)?.observe(viewLifecycleOwner) {
            mBinding.apply {
                word = it?.wip ?: ""
                txtWord.text = it?.wip
                txtMeaning.text = it?.meaning
                txtSampleSentence.text = it?.sampleSentence
                tvTags.text = it?.customTag?.joinToString(", ")
                it?.customTag
                if (it.readCount != null) txtReadCount.text = it.readCount?.toInt().toString() + " times" else txtReadCount.text = "0 times"
                if (it.displayCount != null) txtViewCount.text = it.displayCount?.toInt().toString() + " times" else txtViewCount.text = "0 times"

            }
        }
        val viewCount = intent?.getFloat("view_count", 0f)
        if (viewCount != null) {
            if(SharedPref.isAppLaunched(requireContext())) {
//                updateViewedCount(viewCount)
            }
        }

        mBinding.imgBack.setOnClickListener {
//            updateReadCount()
            findNavController().navigateUp()
        }

        mBinding.btnEdit.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("wip_id", id)
            findNavController().navigate(R.id.WIPEditFragment, bundle)
        }

        mBinding.tvDeleteWIP.setOnClickListener {
            wipViewModel.deleteWIPById(id)
            lifecycleScope.launch {
//                delay(500)
                sharedViewModel.isWIPDeleted = true
                findNavController().navigateUp()
            }
        }

        mBinding.dEncountered.setOnClickListener {
            wipViewModel.resetEncountered(id)
            mBinding.txtReadCount.text = "0"
        }

        mBinding.dViewed.setOnClickListener {
            wipViewModel.resetViewed(id)
            mBinding.txtViewCount.text = "0"
        }
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS){
                Log.d("TAG", "Initialization Success")
            }else{
                Log.d("TAG", "Initialization Failed")
            }
        }
        textToSpeech.language = Locale.US
        mBinding.ivSpeaker.setOnClickListener {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null)
        }

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                updateReadCount()
//                findNavController().navigateUp()
//            }
//
//        })
    }

    private fun updateReadCount() {
        var readCount = mBinding.txtReadCount.text.substring(0, mBinding.txtReadCount.text.length - 5).trim().toInt()
        readCount += 1
        wipViewModel.updateReadCount(id = id, readCount = readCount.toFloat())
    }

    private fun updateViewedCount(viewCount: Float) {
        val count = viewCount.toInt() + 1
        wipViewModel.updateViewedCount(id = id, viewCount = count.toFloat())
    }
    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.shutdown()
    }

}
