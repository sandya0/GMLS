package com.example.gmls.data.local

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(data: String): List<String> = if (data.isEmpty()) emptyList() else data.split(",")
} 
