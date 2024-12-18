package com.example.quickqrapp.presentation.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: String = "",
    val scanHistory: List<Scan> = emptyList()
)

data class Scan(
    val type: String = "",
    val qrType: String = "",
    val data: String = "",
    val timestamp: String = ""
)
