package com.example.quickqrapp.presentation.model

data class ScanHistory(
    val userId: String = "",
    val type: String = "",
    val qrType: String = "",
    val data: Any? = null,
    val createdAt: String = ""
)

data class HistoryItem(
    val documentId: String,
    val createdAt: String,
    val type: String,
    val qrType: String,
    val data: Any
)

enum class QRType {
    TEXT, LINK, EMAIL, GEO, WIFI, UNKNOWN
}

data class QRData(
    val type: QRType,
    val displayText: String,
    val rawData: Any
)