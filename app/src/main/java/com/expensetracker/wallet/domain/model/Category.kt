package com.expensetracker.wallet.domain.model

data class CategoryGroup(
    val id: String,
    val name: String,
    val color: String,
    val icon: String?,
    val type: CategoryType,
    val sortOrder: Int,
    val isSystem: Boolean,
)

data class Category(
    val id: String,
    val groupId: String,
    val name: String,
    val icon: String?,
    val sortOrder: Int,
    val isSystem: Boolean,
)

data class Label(
    val id: String,
    val name: String,
    val color: String,
    val createdAt: Long,
)
