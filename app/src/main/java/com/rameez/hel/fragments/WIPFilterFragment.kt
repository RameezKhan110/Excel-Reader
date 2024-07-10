package com.rameez.hel.fragments

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rameez.hel.R
import com.rameez.hel.adapter.CategoryAdapter
import com.rameez.hel.adapter.CustomTagsAdapter
import com.rameez.hel.databinding.FragmentWIPFilterBinding
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


class WIPFilterFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPFilterBinding
    private lateinit var customTagAdapter: CustomTagsAdapter
    private val wipViewModel: WIPViewModel by activityViewModels()
    private val customTags = mutableListOf<String>()
    private lateinit var filteredCategoryList: MutableList<String>
    private lateinit var filteredTagsList: MutableList<String>
    private var filteredReadCount: Float? = null
    private var filteredViewedCount: Float? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val categoryAdapter = CategoryAdapter()
    private val categoriesList = arrayListOf<String>()
    private var readOperator: String? = null
    private var viewedOperator: String? = null
    private var isFirstTime = false
    private var filteredWIP: String? = null
    private var filteredMeaning: String? = null
    private var filteredSampleSen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customTagAdapter = CustomTagsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (::mBinding.isInitialized.not()) {
            isFirstTime = true
            mBinding = FragmentWIPFilterBinding.inflate(layoutInflater, container, false)
        }
        mBinding.apply {
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filteredCategoryList = sharedViewModel.categoryList
        filteredTagsList = sharedViewModel.tagsList
        filteredReadCount = sharedViewModel.readCount
        filteredViewedCount = sharedViewModel.viewedCount
        readOperator = sharedViewModel.readOperator
        viewedOperator = sharedViewModel.viewedOperator
        filteredWIP = sharedViewModel.filteredWord
        filteredMeaning = sharedViewModel.filteredMeaning
        filteredSampleSen = sharedViewModel.filteredSampleSen
        mBinding.etReadCount.text = null
        mBinding.etTimer.text = ""
        Log.d("TAG", "onCreate: ")

        setUpRecyclerView()

        Log.d("TAG", "onViewCreated")

        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) {
            it.forEach { wipItem ->

                val lowerCaseCategory = wipItem.category?.lowercase(Locale.ROOT)
                if (wipItem.category !in categoriesList && lowerCaseCategory !in categoriesList.map {
                        it.lowercase(
                            Locale.ROOT
                        )
                    }
                ) {
                    wipItem.category?.let { it1 -> categoriesList.add(it1) }
                }

                wipItem.customTag?.filterNot {
                    it.isEmpty()
                }?.forEach { customTag ->
                    val lowerCaseCustomTag =
                        customTag.lowercase(Locale.ROOT)
                    if (customTag !in customTags && lowerCaseCustomTag !in customTags.map { it.lowercase() }) {
                        customTags.add(customTag)
                    }
                }

            }
            readOperatorSetup()
            viewedOperatorSetup()
            categoryAdapter.submitList(categoriesList)
            customTagAdapter.submitList(customTags)
        }

        mBinding.apply {

            btnTimer.setOnClickListener {
                val timePicker = com.rameez.hel.utils.TimePicker()
                timePicker.setTitle("Select time")
                //timePicker.includeHours = false
                timePicker.setOnTimeSetOption("Set time") { hour, minute, second ->

                    if (hour != 0 || minute != 0 || second != 0) {
                        mBinding.etTimer.text = "$hour:$minute:$second"
                        sharedViewModel.selectedHours = hour
                        sharedViewModel.selectedMins = minute
                        sharedViewModel.selectedSecs = second
                    } else {
                        Toast.makeText(requireContext(), "Time can't be zero", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                /* To show the dialog you have to supply the "fragment manager"
                    and a tag (whatever you want)
                 */
                timePicker.show(requireActivity().supportFragmentManager, "time_picker")
//                showTimePickerDialog()
            }

            btnClearFilters.setOnClickListener {
                filteredCategoryList.clear()
                filteredTagsList.clear()
                filteredReadCount = null
                filteredViewedCount = null
                Toast.makeText(requireContext(), "All filters are cleared", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
//            cbWord.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    filteredCategoryList.add(cbWord.text.toString())
//                } else {
//                    filteredCategoryList.remove(cbWord.text.toString())
//                }
//            }
//
//            cbPhrases.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    filteredCategoryList.add(cbPhrases.text.toString())
//                } else {
//                    filteredCategoryList.remove(cbPhrases.text.toString())
//                }
//            }
//
//            cbIdioms.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    filteredCategoryList.add(cbIdioms.text.toString())
//                } else {
//                    filteredCategoryList.remove(cbIdioms.text.toString())
//                }
//            }

            btnApplyFilter.setOnClickListener {

                if (etReadCount.text?.isNotBlank() == true) {
                    val readCount = etReadCount.text?.toString()?.trim()?.toFloat()
                    filteredReadCount = readCount
                }
                if (etViewedCount.text?.isNotBlank() == true) {
                    val viewedCount = etViewedCount.text?.toString()?.trim()?.toFloat()
                    filteredViewedCount = viewedCount
                }

                if (etWIP.text?.isNotBlank() == true) {
                    filteredWIP = etWIP.text?.toString()?.trim()
                }

                if (etMeaning.text?.isNotBlank() == true) {
                    filteredMeaning = etMeaning.text?.toString()?.trim()
                }

                if (etSampleSen.text?.isNotBlank() == true) {
                    filteredSampleSen = etSampleSen.text?.toString()?.trim()
                }

                wipViewModel.getWIPs()?.observe(viewLifecycleOwner) { data ->

                    if (filteredCategoryList.isNotEmpty() ||
                        filteredTagsList.isNotEmpty() ||
                        filteredReadCount != null ||
                        filteredViewedCount != null ||
                        filteredWIP != null ||
                        filteredMeaning != null ||
                        filteredSampleSen != null) {

                        var filteredData = data
                        CoroutineScope(Dispatchers.IO).launch {

                            if(filteredWIP != null) {
                                filteredData = filteredData.filter { wipItem ->
                                    wipItem.wip == filteredWIP
                                }
                            }

                            if(filteredMeaning != null && filteredData.isNotEmpty()) {
                                filteredData = filteredData.filter { wipItem ->
                                    wipItem.meaning == filteredMeaning
                                }
                            }

                            if(filteredSampleSen != null && filteredData.isNotEmpty()) {
                                filteredData = filteredData.filter { wipItem ->
                                    wipItem.sampleSentence == filteredSampleSen
                                }
                            }


                            if (filteredData.isNotEmpty() && filteredCategoryList.isNotEmpty()) {
                                filteredData = filteredData.filter { wipItem ->
                                    wipItem.category?.lowercase(Locale.ROOT) in filteredCategoryList.map {
                                        it.lowercase(
                                            Locale.ROOT
                                        )
                                    }
                                }
                            }

                            // If the filtered list is not empty after category filtering, proceed to filter by tags
                            if (filteredData.isNotEmpty() && filteredTagsList.isNotEmpty()) {
                                filteredData = filteredData.filter { wipItem ->
                                    wipItem.customTag?.any { tag ->
                                        tag.lowercase(Locale.ROOT) in filteredTagsList.map {
                                            it.lowercase(
                                                Locale.ROOT
                                            )
                                        }
                                    }
                                        ?: false
                                }
                            }

                            if (filteredData.isNotEmpty() && filteredReadCount != null) {
                                filteredData = when (readOperator) {
                                    "=" -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.readCount == filteredReadCount
                                        }
                                    }

                                    ">" -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.readCount!! > filteredReadCount!!
                                        }
                                    }

                                    else -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.readCount!! < filteredReadCount!!
                                        }
                                    }
                                }
                            }

                            if (filteredData.isNotEmpty() && filteredViewedCount != null) {
                                filteredData = when (viewedOperator) {
                                    "=" -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.displayCount == filteredViewedCount
                                        }
                                    }

                                    ">" -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.displayCount!! > filteredViewedCount!!
                                        }
                                    }

                                    else -> {
                                        filteredData.filter { wipItem ->
                                            wipItem.displayCount!! < filteredViewedCount!!
                                        }
                                    }
                                }
                            }

                            sharedViewModel.filteredWipsList = filteredData.toMutableList()
                            sharedViewModel.isReadAloud = switchMaterial.isChecked
                            filteredData.forEach {
                                Log.d("TAG", "filteredWips $it")
                            }
                        }
                    }

                    if (filteredCategoryList.isEmpty() && filteredTagsList.isEmpty() && filteredReadCount == null && filteredViewedCount == null) {
                        Toast.makeText(requireContext(), "Nothing to filter", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        if (filteredReadCount != null) {
                            if (readOperator == null) {
                                Toast.makeText(
                                    requireContext(),
                                    "Please choose operator",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (filteredViewedCount != null) {
                                    if (viewedOperator == null) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose operator",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        findNavController().navigate(R.id.carouselFragment)
                                    }
                                } else {
                                    findNavController().navigate(R.id.carouselFragment)
                                }
                            }
                        } else if (filteredViewedCount != null) {
                            if (viewedOperator == null) {
                                Toast.makeText(
                                    requireContext(),
                                    "Please choose operator",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (filteredReadCount != null) {
                                    if (readOperator == null) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose operator",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        findNavController().navigate(R.id.carouselFragment)
                                    }
                                } else {
                                    findNavController().navigate(R.id.carouselFragment)
                                }
                            }
                        } else {
                            findNavController().navigate(R.id.carouselFragment)
                        }
                    }
                }
            }
        }

        customTagAdapter.onCheckBoxClicked = { tagValue, isChecked, position ->
            if (isChecked) {
                filteredTagsList.add(tagValue)
            } else {
                filteredTagsList.remove(tagValue)
            }
        }

        categoryAdapter.onCheckBoxClicked = { tagValue, isChecked, position ->
            if (isChecked) {
                filteredCategoryList.add(tagValue)
            } else {
                filteredCategoryList.remove(tagValue)
            }
        }
    }

    private fun setUpRecyclerView() {
        mBinding.apply {
            rvList.layoutManager = GridLayoutManager(requireContext(), 3)
            rvList.adapter = customTagAdapter
            categoryRv.layoutManager = GridLayoutManager(requireContext(), 3)
            categoryRv.adapter = categoryAdapter
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                mBinding.etTimer.text = "$selectedHour:$selectedMinute"
                sharedViewModel.selectedHours = selectedHour
                sharedViewModel.selectedMins = selectedMinute
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()

        // Hide the second field
        val timePicker = timePickerDialog.findViewById<TimePicker>(
            Resources.getSystem().getIdentifier("timePicker", "id", "android")
        )
        if (timePicker != null) {
            try {
                // Retrieve the child NumberPicker fields for hours and minutes
                val hoursField = timePicker.findViewById<NumberPicker>(
                    Resources.getSystem().getIdentifier("hour", "id", "android")
                )
                val minutesField = timePicker.findViewById<NumberPicker>(
                    Resources.getSystem().getIdentifier("minute", "id", "android")
                )

                // Hide the second field
                val secondsField = timePicker.findViewById<NumberPicker>(
                    Resources.getSystem().getIdentifier("second", "id", "android")
                )
                secondsField?.visibility = View.GONE

                // Hide the colon separator between hours and minutes
                val dividerField = timePicker.findViewById<View>(
                    Resources.getSystem().getIdentifier("divider", "id", "android")
                )
                dividerField?.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readOperatorSetup() {

        val readCountsStr = mutableListOf("=", "<", ">")
        readCountsStr.add(0, "")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, readCountsStr)
        mBinding.readSpinner.setSelection(0)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.readSpinner.adapter = spinnerAdapter
        mBinding.readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val selectedItem = parent.getItemAtPosition(position)
                    if (selectedItem.toString() != "") {
                        readOperator = selectedItem.toString()
                        Log.d("TAG", "read count $filteredReadCount")
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun viewedOperatorSetup() {

        val viewedCountsStr = mutableListOf("=", "<", ">")
        viewedCountsStr.add(0, "")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, viewedCountsStr)
        mBinding.viewedSpinner.setSelection(0)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.viewedSpinner.adapter = spinnerAdapter
        mBinding.viewedSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        val selectedItem = parent.getItemAtPosition(position)
                        if (selectedItem.toString() != "") {
                            viewedOperator = selectedItem.toString()
                            Log.d("TAG", "read count $viewedOperator")
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
    }
}
