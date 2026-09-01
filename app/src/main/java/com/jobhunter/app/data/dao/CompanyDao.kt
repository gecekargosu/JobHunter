package com.jobhunter.app.data.dao

import androidx.room.*
import com.jobhunter.app.models.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Query("SELECT * FROM companies ORDER BY createdAt DESC")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE name LIKE '%' || :query || '%' OR industry LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchCompanies(query: String): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE email != '' AND email IS NOT NULL")
    fun getContactableCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): Company?

    @Query("SELECT * FROM companies WHERE domain = :domain")
    suspend fun getCompanyByDomain(domain: String): Company?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<Company>)

    @Update
    suspend fun updateCompany(company: Company)

    @Delete
    suspend fun deleteCompany(company: Company)

    @Query("DELETE FROM companies WHERE id = :id")
    suspend fun deleteCompanyById(id: Long)

    @Query("SELECT COUNT(*) FROM companies")
    fun getCompanyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM companies WHERE email != '' AND email IS NOT NULL")
    fun getContactableCount(): Flow<Int>
}
