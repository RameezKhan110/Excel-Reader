package com.rameez.hel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rameez.hel.data.WIPDatabase
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.data.repository.WIPRepository
import kotlinx.coroutines.launch

class WIPViewModel : ViewModel() {

    private val wipDao = WIPDatabase.getDatabase()?.wipDao()
    private val wipRepository = wipDao?.let { WIPRepository(it) }

    fun insertWIP(wipItem: WIPModel) = viewModelScope.launch {
        wipRepository?.insertWIP(wipItem)
    }

    fun getWIPs(): LiveData<List<WIPModel>>? {
        return wipRepository?.getWIPs()
    }

    fun dropTable() = viewModelScope.launch {
        wipRepository?.dropTable()
    }

    fun getWIPById(id: Int): LiveData<WIPModel>? {
        return wipRepository?.getWIPById(id)
    }

    fun updateWIP(
        id: Int,
        category: String,
        wip: String,
        meaning: String,
        sampleSentence: String,
        customTag: List<String>,
        readCount: Float,
        viewedCount: Float
    ) = viewModelScope.launch {
        wipRepository?.updateWIP(
            id,
            category,
            wip,
            meaning,
            sampleSentence,
            customTag,
            readCount,
            viewedCount
        )
    }

    fun updateReadCount(id: Int, readCount: Float) = viewModelScope.launch {
        wipRepository?.updateReadCount(id, readCount)
    }

    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>>? {
        return wipRepository?.getWIPsWithCustomTag(tag)
    }
}