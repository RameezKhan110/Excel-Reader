package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rameez.hel.R
import com.rameez.hel.adapter.WIPSearchAdapter
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentWIPSearchBinding
import com.rameez.hel.viewmodel.WIPViewModel


class WIPSearchFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPSearchBinding
    private val wipViewModel: WIPViewModel by activityViewModels()
    private val wipSearchAdapter = WIPSearchAdapter()
    private var wipList = listOf<WIPModel>()
    private var filteredList = arrayListOf<WIPModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWIPSearchBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) {
            wipList = it
        }

        mBinding.etSearch.requestFocus()
        showSoftKeyboard()

        mBinding.etSearch.doAfterTextChanged {
            mBinding.noData.visibility = View.GONE
            if (it.isNullOrEmpty()) {
                mBinding.noData.visibility = View.VISIBLE
            }
            val wordFilteredList = wipList.filter { wipItem ->
                it?.let { filterValue ->
                    filterValue.isNotEmpty() && wipItem.wip?.contains(filterValue, ignoreCase = true) == true
                } ?: false
            }
            val meaningFilteredList = wipList.filter { wipItem ->
                it?.let { filterValue ->
                    filterValue.isNotEmpty() && wipItem.meaning?.contains(filterValue, ignoreCase = true) == true
                } ?: false
            }
            val sampleSentenceFilteredList = wipList.filter { wipItem ->
                it?.let { filterValue ->
                    filterValue.isNotEmpty() && wipItem.sampleSentence?.contains(filterValue, ignoreCase = true) == true
                } ?: false
            }

            filteredList = (wordFilteredList + meaningFilteredList + sampleSentenceFilteredList) as ArrayList<WIPModel>

            wipSearchAdapter.submitList(filteredList.distinct())
            wipSearchAdapter.notifyDataSetChanged()
        }

        wipSearchAdapter.onWipItemClicked = {
            val bundle = Bundle()
            bundle.putInt("wip_id", it)
            findNavController().navigate(R.id.WIPDetailFragment, bundle)
        }
    }

    private fun setUpRecyclerView() {
        mBinding.apply {
            rvList.layoutManager = LinearLayoutManager(requireContext())
            rvList.adapter = wipSearchAdapter
        }
    }

    private fun showSoftKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(mBinding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }
}

