package com.rameez.hel.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.R
import com.rameez.hel.SharedPref
import com.rameez.hel.adapter.CarouselAdapter
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentCarouselBinding
import com.rameez.hel.viewmodel.ShardViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import java.util.concurrent.TimeUnit

class CarouselFragment : Fragment() {

    private lateinit var mBinding: FragmentCarouselBinding
    private val carouselAdapter = CarouselAdapter()
    private val sharedViewModel: ShardViewModel by activityViewModels()
    private val wipViewModel: WIPViewModel by activityViewModels()
    private var currentPosition: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if(carouselAdapter.currentList.size -1== currentPosition) {
                currentPosition = 0
                mBinding.rvList.smoothScrollToPosition(currentPosition)
            } else {
                if(currentPosition == 0) {
                    currentPosition = 1
                } else {
                    mBinding.rvList.smoothScrollToPosition(currentPosition+1)
                }
            }
            handler.postDelayed(this, 5000)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCarouselBinding.inflate(layoutInflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        SharedPref.isFilterScreenCancelled(requireContext(), false)
        sharedViewModel.categoryList.clear()
        sharedViewModel.tagsList.clear()
        sharedViewModel.readCount = null
        sharedViewModel.viewedCount = null
        sharedViewModel.readCount = null
        sharedViewModel.viewedOperator = null


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    sharedViewModel.selectedMins = null
                    sharedViewModel.selectedHours = null
                    sharedViewModel.isTimerRunning = false
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
                findNavController().navigateUp()
            }
            rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvList.adapter = carouselAdapter
        }

        val idsList = arrayListOf<Int>()
        sharedViewModel.filteredWipsList.forEach {
            it.id?.let { it1 -> idsList.add(it1) }
        }

        var newList: List<WIPModel>
        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { wipsList ->
            newList = wipsList.filter {
                it.id in idsList
            }
            val shuffledList = newList.shuffled()
            Log.d("TAG", "shuffled $shuffledList")
            carouselAdapter.submitList(shuffledList)
        }

        carouselAdapter.onItemClick = { id ->
            val bundle = Bundle()
            bundle.putInt("wip_id", id)
            findNavController().navigate(R.id.WIPDetailFragment, bundle)
        }

        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val layoutManager = recyclerView.layoutManager  as LinearLayoutManager
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    Log.d("TAG", "First visible item position: $firstVisibleItemPosition")
                    Log.d("TAG", "Last visible item position: $lastVisibleItemPosition")
                    currentPosition = firstVisibleItemPosition
                }
            }
        })
        handler.post(runnable)
    }

    private fun startTimer() {
        if (sharedViewModel.selectedHours != null && sharedViewModel.selectedMins != null) {
            mBinding.tvTimer.visibility = View.VISIBLE
            val totalTimeInMillis = sharedViewModel.selectedHours?.toLong()
                ?.let { TimeUnit.HOURS.toMillis(it) }
                ?.plus(TimeUnit.MINUTES.toMillis(sharedViewModel.selectedMins?.toLong() ?: 0))

            object : CountDownTimer(totalTimeInMillis!!, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                    val formattedTime =
                        String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
                    mBinding.tvTimer.text = formattedTime
                }

                override fun onFinish() {
                    if (isAdded && activity != null) {
                        mBinding.tvTimer.text = "00:00:00"
                        Toast.makeText(requireContext(), "Timer Finished", Toast.LENGTH_SHORT)
                            .show()
                        sharedViewModel.selectedHours = null
                        sharedViewModel.selectedMins = null
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
}