package com.rameez.hel.data.repository

import androidx.lifecycle.LiveData
import com.rameez.hel.data.WIPDao
import com.rameez.hel.data.model.WIPModel

class WIPRepository(private val wipDao: WIPDao) {

    suspend fun insertWIP(wipItem: WIPModel) {
        wipDao.insertWIP(wipItem)
    }

    fun getWIPs(): LiveData<List<WIPModel>> {
        return wipDao.getWIPs()
    }

    suspend fun getWIPs2(): List<WIPModel> {
        return wipDao.getWIPs2()
    }
    suspend fun dropTable() {
        wipDao.dropTable()
    }

    fun getWIPById(id: Int): LiveData<WIPModel> {
        return wipDao.getWIPById(id)
    }

    suspend fun updateWIP(
        id: Int,
        category: String,
        wip: String,
        meaning: String,
        sampleSentence: String,
        customTag: List<String>,
        readCount: Float,
        viewedCount: Float
    ) {
        wipDao.updateWIP(id, category,  wip, meaning, sampleSentence, customTag, readCount, viewedCount)
    }

    suspend fun updateReadCount(id: Int, readCount: Float) {
        wipDao.updateReadCount(id, readCount)
    }

    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>> {
        return wipDao.getWIPsWithCustomTag(tag)
    }

    suspend fun updateViewedCount(id: Int, viewCount: Float) {
        wipDao.updateViewedCount(id, viewCount)
    }

    suspend fun deleteWIPById(id: Int) {
        wipDao.deleteWIPbyId(id)
    }

    suspend fun deleteWholeCategory(categories: List<String?>) {
        wipDao.deleteWholeCategory(categories)
    }

}