package com.example.quickqrapp.presentation.history

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickqrapp.R
import com.example.quickqrapp.presentation.model.HistoryItem
import com.example.quickqrapp.ui.theme.Black
import com.example.quickqrapp.ui.theme.Blue
import com.example.quickqrapp.ui.theme.Gray
import com.example.quickqrapp.ui.theme.Light
import com.example.quickqrapp.ui.theme.Red
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen() {
    var scanHistory by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var generateHistory by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    val user = remember { Firebase.auth.currentUser }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        user?.let { currentUser ->
            val db = FirebaseFirestore.getInstance()
            db.collection("scan_history")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Помилка завантаження історії", Toast.LENGTH_SHORT)
                            .show()
                        return@addSnapshotListener
                    }

                    val scannedItems = mutableListOf<HistoryItem>()
                    val generatedItems = mutableListOf<HistoryItem>()

                    snapshot?.documents?.forEach { doc ->
                        val item = HistoryItem(
                            documentId = doc.id,
                            createdAt = doc.getString("createdAt") ?: "",
                            type = doc.getString("type") ?: "",
                            qrType = doc.getString("qrType") ?: "",
                            data = doc.get("data") ?: ""
                        )

                        if (item.type == "scan") {
                            scannedItems.add(item)
                        } else {
                            generatedItems.add(item)
                        }
                    }

                    scanHistory = scannedItems
                    generateHistory = generatedItems
                }
        }
    }

    fun deleteHistoryItem(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("scan_history")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Запис видалено", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Підтвердження") },
            text = { Text("Ви впевнені, що хочете видалити цей запис?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { deleteHistoryItem(it) }
                    showDeleteDialog = null
                }) {
                    Text("Видалити")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Скасувати")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 15.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier = Modifier.clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Light,
            contentColor = Blue,
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(text = "Скановані", fontSize = 16.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(text = "Згенеровані", fontSize = 16.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentItems = if (selectedTab == 0) scanHistory else generateHistory

            itemsIndexed(currentItems) { _, item ->
                HistoryItemCard(
                    item = item,
                    onDeleteClick = { showDeleteDialog = item.documentId }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Gray),
        colors = CardDefaults.cardColors(containerColor = Light)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDateTime(item.createdAt),
                    fontSize = 14.sp,
                    color = Gray
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Видалити",
                        tint = Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Тип: ${item.qrType}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            when {
                item.data is Map<*, *> -> {
                    when (item.qrType) {
                        "Wi-Fi" -> {
                            Text("Назва мережі: ${item.data["name"]}")
                            Text("Пароль: ${item.data["password"]}")
                        }

                        "Email" -> {
                            Text("Адреса: ${item.data["address"]}")
                            Text("Тема: ${item.data["subject"]}")
                            Text("Текст: ${item.data["body"]}")
                        }

                        "Гео" -> {
                            Text("Широта: ${item.data["latitude"]}")
                            Text("Довгота: ${item.data["longitude"]}")
                        }
                    }
                }

                else -> {
                    Text(
                        text = item.data.toString(),
                        fontSize = 16.sp,
                        color = Black
                    )
                }
            }
        }
    }
}

private fun formatDateTime(dateTime: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        return outputFormat.format(date!!)
    } catch (e: Exception) {
        return dateTime
    }
}