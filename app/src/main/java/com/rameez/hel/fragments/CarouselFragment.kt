package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.R
import com.rameez.hel.adapter.CarouselAdapter
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentCarouselBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class CarouselFragment : Fragment() {

    private lateinit var mBinding: FragmentCarouselBinding
    private val carouselAdapter = CarouselAdapter()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val wipViewModel: WIPViewModel by activityViewModels()
    private var currentPosition: Int = 0
    private var previousPosition = 0
    private var isAdded = false
    private val handler = Handler(Looper.getMainLooper())
    private var shuffledList = listOf<WIPModel>()
    private var isFirstTime = false
    private val snapHelper = PagerSnapHelper()
    private lateinit var textToSpeech: TextToSpeech


    private val runnable = object : Runnable {
        override fun run() {
            if (carouselAdapter.currentList.size - 1 == currentPosition) {
                currentPosition = 0
                mBinding.rvList.smoothScrollToPosition(currentPosition)
            } else {
                if (currentPosition == 0) {
                    currentPosition = 1
                } else {
                    mBinding.rvList.smoothScrollToPosition(currentPosition + 1)
                }
            }
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TAG", "Initialization Success")
            } else {
                Log.d("TAG", "Initialization Failed")
            }
        }
        textToSpeech.language = Locale.US
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (::mBinding.isInitialized.not()) {
            isFirstTime = true
            mBinding = FragmentCarouselBinding.inflate(layoutInflater, container, false)
        }
        return mBinding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        SharedPref.isFilterScreenCancelled(requireContext(), false)
        sharedViewModel.categoryList.clear()
        sharedViewModel.tagsList.clear()
        sharedViewModel.readCount = null
        sharedViewModel.viewedCount = null
        sharedViewModel.readCount = null
        sharedViewModel.viewedOperator = null
        sharedViewModel.filteredWord = null
        sharedViewModel.filteredMeaning = null
        sharedViewModel.filteredSampleSen = null



        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    sharedViewModel.selectedMins = null
                    sharedViewModel.selectedHours = null
                    sharedViewModel.isTimerRunning = false
                    sharedViewModel.leftSwipedItemList.clear()
                    sharedViewModel.itemPos = null
                    sharedViewModel.itemId = null
                    findNavController().navigateUp()
                }

            })

        if (sharedViewModel.isTimerRunning.not()) {
            startTimer()
        }

        mBinding.apply {

            ivCancel.setOnClickListener {
//                SharedPref.isFilterScreenCancelled(requireContext(), true)
                sharedViewModel.isTimerRunning = false
                sharedViewModel.selectedMins = null
                sharedViewModel.selectedHours = null
                sharedViewModel.leftSwipedItemList.clear()
                sharedViewModel.itemPos = null
                sharedViewModel.itemId = null
                findNavController().navigateUp()
            }
            rvList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            if (isFirstTime) {
                snapHelper.attachToRecyclerView(rvList)
            }

            rvList.adapter = carouselAdapter
        }

        val idsList = arrayListOf<Int>()
        sharedViewModel.filteredWipsList.forEach {
            it.id?.let { it1 -> idsList.add(it1) }
        }

        if (isFirstTime) {
            lifecycleScope.launch {
                val newList: ArrayList<WIPModel>
                val list = wipViewModel.getWIPs2()
                newList = (list?.filter {
                    it.id in idsList
                } ?: emptyList()) as ArrayList<WIPModel>
                shuffledList = newList.shuffled()
                Log.d("TAG", "shuffled $shuffledList")
                carouselAdapter.submitList(shuffledList)

                if (shuffledList.isNotEmpty()) {
                    val id = shuffledList[0].id
                    val viewCount = shuffledList[0].displayCount
                    if (id != null && viewCount != null) {
                        updateViewedCount(id, viewCount)
                        shuffledList[0].displayCount =
                            shuffledList[0].displayCount?.toInt()?.plus(1)
                                ?.toFloat()
                        carouselAdapter.submitList(shuffledList)
                        carouselAdapter.notifyDataSetChanged()
                        if (sharedViewModel.isReadAloud) {
                            Log.d("TAG", "before speak")
                            textToSpeech = TextToSpeech(requireContext()) { status ->
                                if (status == TextToSpeech.SUCCESS) {
                                    textToSpeech.speak(shuffledList[0].wip, TextToSpeech.QUEUE_FLUSH, null)
                                    Log.d("TAG", "Initialization Success")
                                } else {
                                    Log.d("TAG", "Initialization Failed")
                                }
                            }
                            textToSpeech.language = Locale.US
                        }
                        Log.d("TAG", "after speak")
                    }
                }


//                if(sharedViewModel.itemPos != null) {
//                    var position = 0
//                    carouselAdapter.currentList.forEachIndexed{ index, item ->
//                        if(sharedViewModel.itemPos == item.id) {
//                            position = index
//                        }
//                    }
//                    mBinding.rvList.scrollToPosition(position)
//                    sharedViewModel.itemPos = null
//                }
            }

            isFirstTime = false
        }

        if (sharedViewModel.itemId != null) {
            wipViewModel.getWIPById(sharedViewModel.itemId ?: 0)?.observe(viewLifecycleOwner) {
                if (sharedViewModel.itemPos != null) {
                    shuffledList[sharedViewModel.itemPos ?: 0].apply {
                        sr = it.sr
                        category = it.category
                        wip = it.wip
                        meaning = it.meaning
                        sampleSentence = it.sampleSentence
                        customTag = it.customTag
                        readCount = it.readCount
                        displayCount = it.displayCount
                    }
                    carouselAdapter.notifyItemChanged(sharedViewModel.itemPos ?: 0)
                }

            }
        }


        carouselAdapter.onItemClick = { id, pos ->
            sharedViewModel.itemPos = pos
            sharedViewModel.itemId = id
            val bundle = Bundle()
            bundle.putInt("wip_id", id)
            findNavController().navigate(R.id.WIPDetailFragment, bundle)
        }

        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                    Log.d("TAG", "First visible item position: ${layoutManager.findFirstCompletelyVisibleItemPosition()}")
//                    Log.d("TAG", "Last visible item position: $lastVisibleItemPosition")
                    val snapView = snapHelper.findSnapView(layoutManager)
                    Log.d(
                        "TAG",
                        "snapped item pos: ${snapView?.let { layoutManager.getPosition(it) }}"
                    )
                    currentPosition = firstVisibleItemPosition
                    if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {

                        if (sharedViewModel.isReadAloud) {
                            textToSpeech.speak(
                                shuffledList[lastVisibleItemPosition].wip,
                                TextToSpeech.QUEUE_FLUSH,
                                null
                            )
                        }
                        val id = shuffledList[firstVisibleItemPosition].id
                        val viewCount = shuffledList[firstVisibleItemPosition].displayCount
                        if (id != null && viewCount != null && firstVisibleItemPosition != previousPosition) {
                            previousPosition = firstVisibleItemPosition
                            if (isAdded) {
                                updateViewedCount(id, viewCount)
                                shuffledList[firstVisibleItemPosition].displayCount =
                                    shuffledList[firstVisibleItemPosition].displayCount?.toInt()
                                        ?.plus(1)?.toFloat()
                                carouselAdapter.submitList(shuffledList)
                                carouselAdapter.notifyDataSetChanged()
                                isAdded = false
                            }
                        }
                    }
                }
            }

            var isScrolling = false
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isScrolling) {
                    isScrolling = true

                    if (dx < 0) {

                    } else if (dx > 0) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                        Log.d("TAG", "First visible item position: $firstVisibleItemPosition")
//                        Log.d("TAG", "Last visible item position: $lastVisibleItemPosition")
                        val item = carouselAdapter.getWIPItem(lastVisibleItemPosition)
                        if (item !in sharedViewModel.leftSwipedItemList) {
                            isAdded = true
                            sharedViewModel.leftSwipedItemList.add(item)
                        }
                    }

                    // Reset the flag after the scroll event is handled
                    recyclerView.postDelayed({
                        isScrolling = false
                    }, 500) // Adjust this delay as needed
                }
            }
        })
    }

    private fun startTimer() {
        if (sharedViewModel.selectedHours != null && sharedViewModel.selectedMins != null && sharedViewModel.selectedSecs != null) {
            handler.post(runnable)
            mBinding.tvTimer.visibility = View.VISIBLE

            // Convert hours, minutes, and seconds to milliseconds
            val totalTimeInMillis = sharedViewModel.selectedHours?.toLong()
                ?.let { TimeUnit.HOURS.toMillis(it) }
                ?.plus(TimeUnit.MINUTES.toMillis(sharedViewModel.selectedMins?.toLong() ?: 0))
                ?.plus(TimeUnit.SECONDS.toMillis(sharedViewModel.selectedSecs?.toLong() ?: 0))

            object : CountDownTimer(totalTimeInMillis!!, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                    // Format the time to HH:mm:ss
                    val formattedTime =
                        String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
                    mBinding.tvTimer.text = formattedTime
                }

                override fun onFinish() {
                    if(activity != null) {
                        mBinding.tvTimer.text = "00:00:00"
                        Toast.makeText(requireContext(), "Timer Finished", Toast.LENGTH_SHORT).show()
                        sharedViewModel.selectedHours = null
                        sharedViewModel.selectedMins = null
                        sharedViewModel.selectedSecs = null
                        sharedViewModel.isTimerRunning = false
                        findNavController().navigateUp()
                    }

                }
            }.start()
            sharedViewModel.isTimerRunning = true
        } else {
            mBinding.tvTimer.visibility = View.INVISIBLE
        }
    }


//    private fun startTimer() {
//        if (sharedViewModel.selectedHours != null && sharedViewModel.selectedMins != null) {
//            handler.post(runnable)
//            mBinding.tvTimer.visibility = View.VISIBLE
//            val totalTimeInMillis = sharedViewModel.selectedHours?.toLong()
//                ?.let { TimeUnit.HOURS.toMillis(it) }
//                ?.plus(TimeUnit.MINUTES.toMillis(sharedViewModel.selectedMins?.toLong() ?: 0))
//
//            object : CountDownTimer(totalTimeInMillis!!, 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
//                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
//                    val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
//
//                    val formattedTime =
//                        String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
//                    mBinding.tvTimer.text = formattedTime
//                }
//
//                override fun onFinish() {
////                    if (isAdded && activity != null) {
//                        mBinding.tvTimer.text = "00:00:00"
//                        Toast.makeText(requireContext(), "Timer Finished", Toast.LENGTH_SHORT)
//                            .show()
//                        sharedViewModel.selectedHours = null
//                        sharedViewModel.selectedMins = null
//                        sharedViewModel.isTimerRunning = false
//                        findNavController().navigateUp()
////                    }
//                }
//            }.start()
//            sharedViewModel.isTimerRunning = true
//        } else {
//            mBinding.tvTimer.visibility = View.INVISIBLE
//        }
//    }

    private fun updateViewedCount(id: Int, viewCount: Float) {
        val count = viewCount + 1
        wipViewModel.updateViewedCount(id, count)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.shutdown()
//        leftSwipedItemList.clear()
    }

    override fun onResume() {
        super.onResume()
    }
}