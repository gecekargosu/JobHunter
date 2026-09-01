package com.jobhunter.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jobhunter.app.models.Education
import com.jobhunter.app.models.WorkExperience

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromWorkExperienceList(value: List<WorkExperience>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWorkExperienceList(value: String): List<WorkExperience> {
        val type = object : TypeToken<List<WorkExperience>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromEducationList(value: List<Education>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toEducationList(value: String): List<Education> {
        val type = object : TypeToken<List<Education>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
