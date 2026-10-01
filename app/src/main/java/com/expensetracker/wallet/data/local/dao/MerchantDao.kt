package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.wallet.data.local.entity.MerchantAliasEntity
import com.expensetracker.wallet.data.local.entity.MerchantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants ORDER BY canonicalName")
    fun observeAll(): Flow<List<MerchantEntity>>

    @Query(
        "SELECT merchants.* FROM merchants " +
            "INNER JOIN merchant_aliases ON merchants.id = merchant_aliases.merchantId " +
            "WHERE merchant_aliases.alias = :alias LIMIT 1",
    )
    suspend fun findByAlias(alias: String): MerchantEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(merchant: MerchantEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlias(alias: MerchantAliasEntity)
}
