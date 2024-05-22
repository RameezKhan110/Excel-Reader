package com.rameez.hel.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WIP_LIST")
data class WIPModel constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    var sr: Float? = null,
    var category: String? = null,
    var wip: String? = null,
    var meaning: String? = null,
    var sampleSentence: String? = null,
    var customTag: List<String>? = null,
    var readCount: Float? = null,
    var displayCount: Float? = null
) {
    data class Builder(
        var id: Int? = null,
        var sr: Float? = null,
        var category: String? = null,
        var wip: String? = null,
        var meaning: String? = null,
        var sampleSentence: String? = null,
        var customTag: List<String>? = null,
        var readCount: Float? = null,
        var displayCount: Float? = null
    ) {
        fun id(id: Int?) = apply { this.id = id }
        fun sr(sr: Float?) = apply { this.sr = sr }
        fun category(category: String?) = apply { this.category = category }
        fun wip(wip: String?) = apply { this.wip = wip }
        fun meaning(meaning: String?) = apply { this.meaning = meaning }
        fun sampleSentence(sampleSentence: String?) = apply { this.sampleSentence = sampleSentence }
        fun customTag(customTag: List<String>) = apply { this.customTag = customTag }
        fun readCount(readCount: Float) = apply { this.readCount = readCount }
        fun displayCount(displayCount: Float) = apply { this.displayCount = displayCount }

        fun build() = WIPModel(
            id,
            sr,
            category,
            wip,
            meaning,
            sampleSentence,
            customTag,
            readCount,
            displayCount
        )
    }
}

