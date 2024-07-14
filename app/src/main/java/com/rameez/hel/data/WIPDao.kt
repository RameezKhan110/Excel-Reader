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

    @Query("SELECT * FROM WIP_LIST")
    suspend fun getWIPs2(): List<WIPModel>

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

    @Query("UPDATE WIP_LIST SET readCount = :readCount WHERE id = :id")
    suspend fun updateReadCount(id: Int, readCount: Float)

    @Query("SELECT * FROM WIP_LIST WHERE customTag LIKE '%' || :tag || '%'")
    fun getWIPsWithCustomTag(tag: String): LiveData<List<WIPModel>>

    @Query("UPDATE WIP_LIST SET displayCount = :viewCount WHERE id = :id")
    suspend fun updateViewedCount(id: Int, viewCount: Float)

    @Query("DELETE FROM WIP_LIST WHERE id = :id")
    suspend fun deleteWIPbyId(id: Int)

    @Query("DELETE FROM WIP_LIST WHERE category  IN (:categories)")
    suspend fun deleteWholeCategory(categories: List<String?>)

    @Query("UPDATE WIP_LIST SET readCount = :readCount WHERE id = :id")
    suspend fun resetEncountered(readCount: Float, id: Int)

    @Query("UPDATE WIP_LIST SET displayCount = :displayCount WHERE id = :id")
    suspend fun resetViewedCount(displayCount: Float, id: Int)

    @Query("UPDATE WIP_LIST SET readCount = 0.0 WHERE category IN (:categories)")
    suspend fun resetEncounteredForCategories(categories: List<String>)

    @Query("UPDATE WIP_LIST SET displayCount = 0.0 WHERE category IN (:categories)")
    suspend fun resetViewedForCategories(categories: List<String>)
}