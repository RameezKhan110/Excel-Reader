package com.rameez.hel.viewmodel

import androidx.lifecycle.ViewModel
import com.rameez.hel.data.model.WIPModel

class SharedViewModel : ViewModel() {

    var filteredWipsList = mutableListOf<WIPModel>()
    var selectedHours: Int? = null
    var selectedMins: Int? = null
    var selectedSecs: Int?= null
    var tagsList = mutableListOf<String>()
    var categoryList = mutableListOf<String>()
    var readCount: Float? = null
    var viewedCount: Float? = null
    var filteredWord: String? = null
    var filteredMeaning: String? = null
    var filteredSampleSen: String? = null
    var isTimerRunning: Boolean = false
    var readOperator: String? = null
    var viewedOperator: String? = null
    val leftSwipedItemList = arrayListOf<WIPModel>()
    var notDeletedTags: ArrayList<String>? = null
    var itemPos: Int? = null
    var itemId: Int? = null
    var itemPosFromHome: Int? = null
    var itemIdFromHome: Int? = null
    var isReadAloud: Boolean = false
    var isWIPDeleted = false
    var isWipAdded: Boolean = false


}