package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** plan.md §16 */
@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val createdAt: Long,
)
