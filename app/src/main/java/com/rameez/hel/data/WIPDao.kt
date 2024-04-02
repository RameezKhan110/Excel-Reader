package com.rameez.hel.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rameez.hel.data.model.WIPModel

@Dao
interface WIPDao {

    @Insert
    suspend fun insertWIP(wipItem: WIPModel)

    @Query("SELECT * FROM WIP_LIST")
    fun getWIPs(): LiveData<List<WIPModel>>

    @Query("DELETE FROM WIP_LIST")
    suspend fun dropTable()

    @Query("SELECT * FROM WIP_LIST WHERE id= :id")
    fun getWIPById(id: Int): LiveData<WIPModel>

    @Query("UPDATE WIP_LIST SET category = :category, wip = :wip, meaning = :meaning, sampleSentence = :sampleSentence, customTag = :customTag, readCount = :readCount, displayCount = :viewedCount WHERE id = :id")
    suspend fun updateWIP(
        id: Int,
        category: String,
        wip: String,
        meaning: String,
        sampleSentence: String,
        customTag: List<String>,
        readCount: Float,
        viewedCount: Float
    )

    @Query("UPDATE WIP_LIST SET displayCount = :viewCount WHERE id = :id")
    suspend fun updateViewedCount(id: Int, viewCount: Float)

    @Query("SELECT * FROM WIP_LIST WHERE customTag LIKE '%' || :tag || '%'")
    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>>
}