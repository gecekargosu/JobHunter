package com.jobhunter.app.data.dao

import androidx.room.*
import com.jobhunter.app.models.JobListing
import kotlinx.coroutines.flow.Flow

@Dao
interface JobListingDao {

    @Query("SELECT * FROM job_listings ORDER BY createdAt DESC")
    fun getAllJobListings(): Flow<List<JobListing>>

    @Query("SELECT * FROM job_listings WHERE source = :source ORDER BY createdAt DESC")
    fun getJobListingsBySource(source: String): Flow<List<JobListing>>

    @Query("SELECT * FROM job_listings WHERE isSelected = 1")
    fun getSelectedJobs(): Flow<List<JobListing>>

    @Query("SELECT * FROM job_listings WHERE id = :id")
    suspend fun getJobListingById(id: Long): JobListing?

    @Query("SELECT * FROM job_listings WHERE title LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchJobListings(query: String): Flow<List<JobListing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobListing(jobListing: JobListing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobListings(jobListings: List<JobListing>)

    @Update
    suspend fun updateJobListing(jobListing: JobListing)

    @Delete
    suspend fun deleteJobListing(jobListing: JobListing)

    @Query("DELETE FROM job_listings WHERE id = :id")
    suspend fun deleteJobListingById(id: Long)

    @Query("UPDATE job_listings SET isSelected = :isSelected WHERE id = :id")
    suspend fun updateSelection(id: Long, isSelected: Boolean)

    @Query("UPDATE job_listings SET isApplied = 1, appliedAt = :appliedAt WHERE id = :id")
    suspend fun markAsApplied(id: Long, appliedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM job_listings")
    fun getJobListingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_listings WHERE isSelected = 1")
    fun getSelectedJobCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_listings WHERE isApplied = 1")
    fun getAppliedJobCount(): Flow<Int>
}
