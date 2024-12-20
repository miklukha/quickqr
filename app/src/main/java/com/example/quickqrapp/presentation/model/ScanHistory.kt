package com.example.quickqrapp.presentation.model

data class ScanHistory(
    val userId: String = "",
    val type: String = "",
    val qrType: String = "",
    val data: Any? = null,
    val imageUrl: String? = null,
    val createdAt: String = ""
)
