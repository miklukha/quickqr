package com.example.quickqrapp.presentation.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(db: FirebaseFirestore) {
    Text("Home")

}

//data class User(
//    val name: String,
//
//    )
//
//fun createUser() {
//}