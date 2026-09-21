package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wallet.data.local.entity.InstitutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitutionDao {
    @Query("SELECT * FROM institutions ORDER BY name")
    fun observeAll(): Flow<List<InstitutionEntity>>

    @Query("SELECT * FROM institutions WHERE id = :id")
    suspend fun getById(id: String): InstitutionEntity?

    @Query("SELECT * FROM institutions WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): InstitutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(institution: InstitutionEntity)

    @Update
    suspend fun update(institution: InstitutionEntity)
}
