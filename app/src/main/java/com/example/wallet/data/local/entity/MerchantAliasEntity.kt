package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** plan.md §29 */
@Entity(
    tableName = "merchant_aliases",
    primaryKeys = ["merchantId", "alias"],
    foreignKeys = [
        ForeignKey(
            entity = MerchantEntity::class,
            parentColumns = ["id"],
            childColumns = ["merchantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("merchantId")],
)
data class MerchantAliasEntity(
    val merchantId: String,
    val alias: String,
)
