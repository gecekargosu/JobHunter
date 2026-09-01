package com.jobhunter.app.data.dao

import androidx.room.*
import com.jobhunter.app.models.ApplicationStatus
import com.jobhunter.app.models.JobApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {

    @Query("SELECT * FROM job_applications ORDER BY appliedAt DESC")
    fun getAllApplications(): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE status = :status ORDER BY appliedAt DESC")
    fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE companyId = :companyId ORDER BY appliedAt DESC")
    fun getApplicationsByCompany(companyId: Long): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getApplicationById(id: Long): JobApplication?

    @Query("SELECT * FROM job_applications WHERE appliedAt BETWEEN :startDate AND :endDate ORDER BY appliedAt DESC")
    fun getApplicationsByDateRange(startDate: Long, endDate: Long): Flow<List<JobApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplication): Long

    @Update
    suspend fun updateApplication(application: JobApplication)

    @Delete
    suspend fun deleteApplication(application: JobApplication)

    @Query("DELETE FROM job_applications WHERE id = :id")
    suspend fun deleteApplicationById(id: Long)

    @Query("SELECT COUNT(*) FROM job_applications")
    fun getTotalApplicationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_applications WHERE status = :status")
    fun getApplicationCountByStatus(status: ApplicationStatus): Flow<Int>

    @Query("SELECT AVG(atsScore) FROM job_applications WHERE atsScore > 0")
    fun getAverageAtsScore(): Flow<Float?>

    @Query("SELECT * FROM job_applications WHERE status = 'APPLIED' AND appliedAt < :followUpDate")
    suspend fun getApplicationsNeedingFollowUp(followUpDate: Long): List<JobApplication>

    @Query("""
        SELECT strftime('%Y-%m', appliedAt/1000, 'unixepoch') as month, 
               COUNT(*) as count 
        FROM job_applications 
        GROUP BY month 
        ORDER BY month DESC 
        LIMIT 12
    """)
    fun getApplicationsByMonth(): Flow<List<MonthlyApplications>>
}

data class MonthlyApplications(
    val month: String,
    val count: Int
)
