package com.jobhunter.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jobhunter.app.data.dao.CompanyDao
import com.jobhunter.app.data.dao.DocumentDao
import com.jobhunter.app.data.dao.EmailDao
import com.jobhunter.app.data.dao.JobApplicationDao
import com.jobhunter.app.data.dao.JobListingDao
import com.jobhunter.app.data.dao.UserProfileDao
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.Document
import com.jobhunter.app.models.DocumentType
import com.jobhunter.app.models.EmailTemplate
import com.jobhunter.app.models.JobApplication
import com.jobhunter.app.models.JobListing
import com.jobhunter.app.models.SentEmail
import com.jobhunter.app.models.UserProfile

@Database(
    entities = [
        Company::class,
        EmailTemplate::class,
        SentEmail::class,
        UserProfile::class,
        JobApplication::class,
        Document::class,
        JobListing::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JobHunterDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun emailDao(): EmailDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun jobApplicationDao(): JobApplicationDao
    abstract fun documentDao(): DocumentDao
    abstract fun jobListingDao(): JobListingDao

    companion object {
        @Volatile
        private var INSTANCE: JobHunterDatabase? = null

        fun getDatabase(context: Context): JobHunterDatabase {
            return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                JobHunterDatabase::class.java,
                "job_hunter_database"
            ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
