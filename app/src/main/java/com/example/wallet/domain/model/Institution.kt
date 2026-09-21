package com.example.wallet.domain.model

data class Institution(
    val id: String,
    val name: String,
    val type: String,
    val logo: String?,
    val country: String?,
)
