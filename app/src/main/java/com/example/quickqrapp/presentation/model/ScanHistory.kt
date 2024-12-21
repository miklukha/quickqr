package com.example.quickqrapp.presentation.model

data class ScanHistory(
    val userId: String = "",
    val type: String = "",
    val qrType: String = "",
    val data: Any? = null,
    val createdAt: String = ""
)

enum class QRType(val displayName: String) {
    LINK("Лінк"),
    TEXT("Текст"),
    EMAIL("Email"),
    GEO("Гео"),
    WIFI("Wi-Fi"),
    UNKNOWN("Невідомий")
}

data class QRData(
    val type: QRType,
    val displayText: String,
    val rawData: Any
)