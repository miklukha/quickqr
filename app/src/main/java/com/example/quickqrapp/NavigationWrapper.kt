package com.example.quickqrapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.quickqrapp.presentation.home.HomeScreen
import com.example.quickqrapp.presentation.initial.InitialScreen
import com.example.quickqrapp.presentation.login.LoginScreen
import com.example.quickqrapp.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(navigateToLogin = { navHostController.navigate("logIn") },
                navigateToSignUp = { navHostController.navigate("signUp") })
        }
        composable("logIn") {
//            LoginScreen(auth) { navHostController.navigate("home") }
            LoginScreen(
                auth,
                navigateToHome = { navHostController.navigate("home") },
                navController = navHostController
            )
        }
        composable("signUp") {
//            SignUpScreen(auth)
            SignUpScreen(
                auth,
                navigateToHome = { navHostController.navigate("home") },
                navController = navHostController
            )
        }
        composable("home") {
            HomeScreen(db)
        }
    }
}