package com.rameez.hel.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(", ")
    }

    @TypeConverter
    fun toList(data: String): List<String> {
        return data.split(",").map { it.trim() }
    }
}