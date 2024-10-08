package com.rameez.hel.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rameez.hel.R
import com.rameez.hel.SharedPref
import com.rameez.hel.adapter.WIPListAdapter
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.databinding.FragmentWIPListBinding
import com.rameez.hel.utils.PermissionUtils
import com.rameez.hel.viewmodel.SharedViewModel
import com.rameez.hel.viewmodel.WIPViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class WIPListFragment : Fragment() {

    private lateinit var mBinding: FragmentWIPListBinding
    private val wipListAdapter = WIPListAdapter()
    private val wipViewModel: WIPViewModel by activityViewModels()
    private val STORAGE_PERMISSION_CODE = 100
    private val OPEN_FILE_REQUEST_CODE = 200
    private lateinit var permissionUtils: PermissionUtils
    private val wipList = arrayListOf<WIPModel>()
    private var isFirstTime = false
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var shuffledList = arrayListOf<WIPModel>()
    override fun onStart() {
        super.onStart()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        SharedPref.appLaunched(requireContext(), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (::mBinding.isInitialized.not()) {
            isFirstTime = true
            mBinding = FragmentWIPListBinding.inflate(layoutInflater, container, false)
        }
        askForPermission()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        })

        permissionUtils = PermissionUtils(this)
        setUpRecyclerView()

        wipViewModel.getWIPs()?.observe(viewLifecycleOwner) {
            if (isFirstTime) {
                mBinding.rvList.visibility = View.GONE
                mBinding.progressbar.visibility = View.VISIBLE
                shuffledList = it.shuffled() as ArrayList<WIPModel>
                wipListAdapter.submitList(shuffledList)
                isFirstTime = false
                lifecycleScope.launch {
                    delay(500)
                    mBinding.rvList.scrollToPosition(0)
                    mBinding.rvList.visibility = View.VISIBLE
                    mBinding.progressbar.visibility = View.GONE
                }
            }
        }

        if (sharedViewModel.itemIdFromHome != null) {
            if (sharedViewModel.isWIPDeleted.not()) {
                wipViewModel.getWIPById(sharedViewModel.itemIdFromHome ?: 0)
                    ?.observe(viewLifecycleOwner) {
                        if (sharedViewModel.itemPosFromHome != null) {
                            shuffledList[sharedViewModel.itemPosFromHome ?: 0].apply {
                                sr = it.sr
                                category = it.category
                                wip = it.wip
                                meaning = it.meaning
                                sampleSentence = it.sampleSentence
                                customTag = it.customTag
                                readCount = it.readCount
                                displayCount = it.displayCount
                            }
                            wipListAdapter.notifyItemChanged(sharedViewModel.itemPosFromHome ?: 0)
                        }

                    }
            } else {
                isFirstTime = true
                sharedViewModel.isWIPDeleted = false
            }

        }

        if(sharedViewModel.isWipAdded) {
            isFirstTime = true
            sharedViewModel.isWipAdded = false
        }


//        if (SharedPref.isAppLaunched(requireContext())) {
//            lifecycleScope.launch {
//                wipViewModel.getWIPs2()?.forEach {
//                    val incCount = it.displayCount?.toInt()?.plus(1)?.toFloat()
//                    it.id?.let { it1 ->
//                        if (incCount != null) {
//                            wipViewModel.updateViewedCount(it1, incCount)
//                        }
//                    }
//                }
//                SharedPref.appLaunched(requireContext(), false)
//            }
//        }


        mBinding.apply {

            listOrientation.setOnClickListener {
                changeRvOrientation()
            }

            imgImportExport.setOnClickListener {
                showCustomDialog().show()
            }

            llSearch.setOnClickListener {
                sharedViewModel.itemIdFromHome = null
                sharedViewModel.itemPosFromHome = null
                findNavController().navigate(R.id.WIPSearchFragment)
            }

            imgFilter.setOnClickListener {
                sharedViewModel.itemIdFromHome = null
                sharedViewModel.itemPosFromHome = null
                findNavController().navigate(R.id.WIPFilterFragment)
            }
        }
        wipListAdapter.onWipItemClicked = { id, viewCount, pos ->
            sharedViewModel.itemIdFromHome = id
            sharedViewModel.itemPosFromHome = pos
            val bundle = Bundle()
            bundle.putInt("wip_id", id)
            bundle.putFloat("view_count", viewCount)
            findNavController().navigate(R.id.WIPDetailFragment, bundle)
        }

        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisibleItemPosition =
                        layoutManager.findFirstCompletelyVisibleItemPosition()
                    val lastVisibleItemPosition =
                        layoutManager.findLastCompletelyVisibleItemPosition()
                    Log.d("TAG", "First visible item position: $firstVisibleItemPosition")
                    Log.d("TAG", "Last visible item position: $lastVisibleItemPosition")
                }
            }
        })

//        wipListAdapter.onIncViewedCount = { id, viewCount ->
//            wipViewModel.updateViewedCount(id, viewCount)
//        }
    }

    private fun setUpRecyclerView() {
        mBinding.apply {
            rvList.layoutManager = LinearLayoutManager(requireContext())
            rvList.adapter = wipListAdapter
        }
    }

    private fun changeRvOrientation() {

        mBinding.apply {

            listOrientation.setOnClickListener {
                val layoutManager = rvList.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val orientation = layoutManager.orientation
                    if (orientation == LinearLayoutManager.VERTICAL) {
                        rvList.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        rvList.layoutManager = LinearLayoutManager(requireContext())
                    }
                }
            }
        }
    }

    private fun showCustomDialog(): AlertDialog {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_layout, null)

        val importWIP = dialogView.findViewById<TextView>(R.id.rWord)
        val exportWIP = dialogView.findViewById<TextView>(R.id.rPhrase)
        val addWIP = dialogView.findViewById<TextView>(R.id.rIdiom)
        val deleteWIP = dialogView.findViewById<TextView>(R.id.rAllWips)
        val rEncountered = dialogView.findViewById<TextView>(R.id.rEncountered)
        val rViewed = dialogView.findViewById<TextView>(R.id.rViewed)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        importWIP.setOnClickListener {
            alertDialog.dismiss()
            openFileUsingSAF()
        }

        exportWIP.setOnClickListener {
            alertDialog.dismiss()
            wipViewModel.getWIPs()?.observe(viewLifecycleOwner) {
                exportToExcel(it, "HEL${System.currentTimeMillis()}")
            }
        }

        addWIP.setOnClickListener {
            alertDialog.dismiss()
            findNavController().navigate(R.id.WIPEditFragment)
        }

        deleteWIP.setOnClickListener {
            alertDialog.dismiss()
            showDeleteWIPDialog().show()
        }

        rEncountered.setOnClickListener {
            alertDialog.dismiss()
            showResetEncounteredDialog().show()
        }

        rViewed.setOnClickListener {
            alertDialog.dismiss()
            showResetViewedDialog().show()
        }
        return alertDialog
    }

    private fun requestStoragePermission() {

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permission,
                STORAGE_PERMISSION_CODE
            )
        } else {
            // Permission already granted, proceed with file selection
            openFileUsingSAF()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileUsingSAF()

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow permission to import or export files",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openFileUsingSAF() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // Or "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    CoroutineScope(Dispatchers.IO).launch {
//                        wipViewModel.dropTable()
                        readExcelFile(uri)
                    }
                }
            }
        }
    }

    private fun readExcelFile(uri: Uri) {

        val contentResolver: ContentResolver = requireContext().contentResolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            Log.d("TAG", "filledRowCount: ${getFilledRowCount(sheet)}")
            val totalRows = getFilledRowCount(sheet)
            val totalColumns = getTotalNoColumns(sheet)
            Log.d("TAG", "total values $totalRows $totalColumns")

            var i = 1
            while (i <= totalRows - 1) {
                var j = 0
                val wipModelBuilder = WIPModel.Builder()

                Log.d("TAG", "Row $i \n")

                while (j <= totalColumns - 1) {
                    val cellValue = sheet.getRow(i).getCell(j)
                    Log.d("TAG", "cell Values $cellValue")
                    when (j) {
                        0 -> wipModelBuilder.sr((cellValue.toString()).toFloat())
                        1 -> wipModelBuilder.category(cellValue?.toString()?.lowercase())
                        2 -> wipModelBuilder.wip(cellValue?.toString())
                        3 -> wipModelBuilder.meaning(cellValue?.toString())
                        4 -> wipModelBuilder.sampleSentence(cellValue?.toString())
                        5 -> wipModelBuilder.customTag(listOf(cellValue?.toString() ?: ""))
                        6 -> wipModelBuilder.readCount(cellValue?.toString()?.toFloat() ?: 0.0f)
                        7 -> wipModelBuilder.displayCount(cellValue?.toString()?.toFloat() ?: 0.0f)


                    }
                    j++
                }
                val wipModel = wipModelBuilder.build()
                wipViewModel.insertWIP(wipModel)
                i++
            }

        }
        isFirstTime = true
    }

    private fun getFilledRowCount(sheet: Sheet): Int {
        var rowCount = 0
        val iterator = sheet.iterator()
        while (iterator.hasNext()) {
            val row = iterator.next()
            if (isEmptyRow(row)) {
                break
            }
            rowCount++
        }
        return rowCount
    }

    private fun isEmptyRow(row: Row): Boolean {
        val lastCellNum = row.lastCellNum.toInt()
        for (i in 0 until lastCellNum) {
            val cell = row.getCell(i)
            if (cell != null && cell.cellType != CellType.BLANK) {
                return false
            }
        }
        return true
    }

    private fun getTotalNoColumns(sheet: Sheet): Int {
        val firstRow = sheet.getRow(0)
        var i = 0
        var totalNoColumns = 0
        while (i < firstRow.physicalNumberOfCells && firstRow.getCell(i).toString() != "") {
            val cellValue = firstRow.getCell(i).toString()
            totalNoColumns += 1
            i++
        }
        return totalNoColumns
    }

    private fun exportToExcel(data: List<WIPModel>, fileName: String) {

        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Saving file...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Sr")
        headerRow.createCell(1).setCellValue("Category")
        headerRow.createCell(2).setCellValue("WIP")
        headerRow.createCell(3).setCellValue("Meaning")
        headerRow.createCell(4).setCellValue("Sample Sentence")
        headerRow.createCell(5).setCellValue("Custom Tag")
        headerRow.createCell(6).setCellValue("Number of times read in general reading")
        headerRow.createCell(7).setCellValue("Number of times displayed in app")

        var rowIndex = 1
        for (model in data) {
            val row = sheet.createRow(rowIndex++)
            model.sr?.toDouble()?.let { row.createCell(0).setCellValue(it) }
            row.createCell(1).setCellValue(model.category ?: "")
            row.createCell(2).setCellValue(model.wip ?: "")
            row.createCell(3).setCellValue(model.meaning ?: "")
            row.createCell(4).setCellValue(model.sampleSentence ?: "")
            row.createCell(5).setCellValue(model.customTag?.joinToString(", "))
            model.readCount?.toDouble()?.let { row.createCell(6).setCellValue(it) }
            model.displayCount?.toDouble()?.let { row.createCell(7).setCellValue(it) }
        }

        try {
            val documentsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(documentsDir, "$fileName.xlsx")
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            lifecycleScope.launch {
                delay(3000)
                progressDialog.dismiss()
                Toast.makeText(context, "File successfully exported", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                workbook.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun askForPermission() {

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        PermissionUtils(this).requestPermissions(
            permission
        ).onPermissionResult = object : PermissionUtils.OnPermissionResult {
            override fun onPermissionGranted() {
//                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(neverAskAgain: Boolean) {
                Toast.makeText(
                    requireContext(),
                    "Please grant permission to import or export .xlsx files",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDeleteWIPDialog(): AlertDialog {
        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_delete_wip_dialog_kayout, null)

        val dWord = dialogView.findViewById<TextView>(R.id.rWord)
        val dPhrase = dialogView.findViewById<TextView>(R.id.rPhrase)
        val dIdiom = dialogView.findViewById<TextView>(R.id.rIdiom)
        val dAllWips = dialogView.findViewById<TextView>(R.id.rAllWips)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.create()


        dWord.setOnClickListener {
            alertDialog.dismiss()
            val categories = shuffledList.filter { it.category == "word" }.map { it.category }
            wipViewModel.deleteWholeCategory(categories)
            isFirstTime = true
        }

        dPhrase.setOnClickListener {
            alertDialog.dismiss()
            val categories = shuffledList.filter { it.category == "phrase" }.map { it.category }
            wipViewModel.deleteWholeCategory(categories)
            isFirstTime = true
        }

        dIdiom.setOnClickListener {
            alertDialog.dismiss()
            val categories = shuffledList.filter { it.category == "idiom" }.map { it.category }
            wipViewModel.deleteWholeCategory(categories)
            isFirstTime = true
        }

        dAllWips.setOnClickListener {
            alertDialog.dismiss()
            wipViewModel.dropTable()
            isFirstTime = true
        }


        return alertDialog
    }

    private fun showResetEncounteredDialog(): AlertDialog {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_reset_dialog_layout, null)

        val rWord = dialogView.findViewById<CheckBox>(R.id.rWord)
        val rPhrase = dialogView.findViewById<CheckBox>(R.id.rPhrase)
        val rIdiom = dialogView.findViewById<CheckBox>(R.id.rIdiom)
        val bDone = dialogView.findViewById<Button>(R.id.bDone)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        bDone.setOnClickListener {
            val list = arrayListOf<String>()
            if (rWord.isChecked) {
                list.add("word")
            }
            if (rPhrase.isChecked) {
                list.add("phrase")
            }
            if (rIdiom.isChecked) {
                list.add("idiom")
            }

            wipViewModel.resetEncounteredForCategories(list)
            alertDialog.dismiss()
            isFirstTime = true
        }


        return alertDialog
    }


    private fun showResetViewedDialog(): AlertDialog {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_reset_dialog_layout, null)

        val rWord = dialogView.findViewById<CheckBox>(R.id.rWord)
        val rPhrase = dialogView.findViewById<CheckBox>(R.id.rPhrase)
        val rIdiom = dialogView.findViewById<CheckBox>(R.id.rIdiom)
        val bDone = dialogView.findViewById<Button>(R.id.bDone)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        bDone.setOnClickListener {
            val list = arrayListOf<String>()
            if (rWord.isChecked) {
                list.add("word")
            }
            if (rPhrase.isChecked) {
                list.add("phrase")
            }
            if (rIdiom.isChecked) {
                list.add("idiom")
            }

            wipViewModel.resetViewedForCategories(list)
            alertDialog.dismiss()
            isFirstTime = true
        }


        return alertDialog
    }

}


