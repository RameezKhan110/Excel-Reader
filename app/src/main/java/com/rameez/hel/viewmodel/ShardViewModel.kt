package com.rameez.hel.viewmodel

import androidx.lifecycle.ViewModel
import com.rameez.hel.data.model.WIPModel

class ShardViewModel : ViewModel() {

    var filteredWipsList = mutableListOf<WIPModel>()
    var selectedHours: Int? = null
    var selectedMins: Int? = null
    var tagsList = mutableListOf<String>()
    var categoryList = mutableListOf<String>()
    var readCount: Float? = null
    var viewedCount: Float? = null
    var isTimerRunning: Boolean = false
    var readOperator: String? = null
    var viewedOperator: String? = null
    val leftSwipedItemList = arrayListOf<WIPModel>()
    var notDeletedTags: ArrayList<String>? = null


}