package com.jobhunter.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jobhunter.app.models.ApplicationStatus
import com.jobhunter.app.models.DocumentType
import com.jobhunter.app.models.Education
import com.jobhunter.app.models.WorkExperience

class Converters {

    private val gson = Gson()

    // ApplicationStatus converters
    @TypeConverter
    fun fromApplicationStatus(status: ApplicationStatus): String {
        return status.name
    }

    @TypeConverter
    fun toApplicationStatus(value: String): ApplicationStatus {
        return try {
            ApplicationStatus.valueOf(value)
        } catch (e: Exception) {
            ApplicationStatus.APPLIED
        }
    }

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

    // DocumentType converter
    @TypeConverter
    fun fromDocumentType(type: DocumentType): String {
        return type.name
    }

    @TypeConverter
    fun toDocumentType(value: String): DocumentType {
        return try {
            DocumentType.valueOf(value)
        } catch (e: Exception) {
            DocumentType.OTHER
        }
    }

    // Boolean converter
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }

    // Long converter (for timestamps)
    @TypeConverter
    fun fromLong(value: Long?): String {
        return value?.toString() ?: ""
    }

    @TypeConverter
    fun toLong(value: String): Long? {
        return value.toLongOrNull()
    }
}
