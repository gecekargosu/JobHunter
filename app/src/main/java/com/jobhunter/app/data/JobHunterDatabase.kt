package com.jobhunter.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jobhunter.app.data.dao.CompanyDao
import com.jobhunter.app.data.dao.EmailDao
import com.jobhunter.app.data.dao.UserProfileDao
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.EmailTemplate
import com.jobhunter.app.models.SentEmail
import com.jobhunter.app.models.UserProfile

@Database(
    entities = [
        Company::class,
        EmailTemplate::class,
        SentEmail::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JobHunterDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun emailDao(): EmailDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: JobHunterDatabase? = null

        fun getDatabase(context: Context): JobHunterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobHunterDatabase::class.java,
                    "job_hunter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
