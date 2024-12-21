package com.example.quickqrapp.presentation.home

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quickqrapp.R
import com.example.quickqrapp.ui.theme.*
import kotlinx.coroutines.launch
import com.example.quickqrapp.presentation.generate.GenerateScreen
import com.example.quickqrapp.presentation.scan.ScanScreen
import com.example.quickqrapp.presentation.history.HistoryScreen
import androidx.compose.material3.ButtonDefaults
import com.google.firebase.Firebase

sealed class BottomNavItem(val route: String, val icon: Int, val label: String) {
    object Home : BottomNavItem("home", R.drawable.home, "Головна")
    object History : BottomNavItem("history", R.drawable.history, "Історія")
    object Generate : BottomNavItem("generate", R.drawable.generate, "Генерувати")
    object Scan : BottomNavItem("scan", R.drawable.scan, "Сканувати")
}

@Composable
fun MainScreen(
    navigateToInitial: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeContent(navigateToInitial = navigateToInitial)
            }
            composable(BottomNavItem.History.route) {
                HistoryScreen()
            }
            composable(BottomNavItem.Generate.route) {
                GenerateScreen()
            }
            composable(BottomNavItem.Scan.route) {
                ScanScreen()
            }
        }
    }
}

@Composable
fun HomeContent(navigateToInitial: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val user = remember { Firebase.auth.currentUser }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier = Modifier.clip(CircleShape)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Легка робота з",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "QR-code",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        if (user != null) {
            Text(
                text = user.email ?: "Email не доступний",
                fontSize = 20.sp,
                color = Black
            )
        } else {
            Log.i("user", "No user is currently logged in.")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    auth.signOut()
                    navigateToInitial()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp)
                .border(1.dp, Red, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Light)
        ) {
            Text(
                text = "Вийти з акаунта",
                color = Red,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Scan,
        BottomNavItem.Generate,
        BottomNavItem.History,
    )


    Box(
        modifier = Modifier
            .background(Light)
            .height(70.dp)
            .drawBehind {
                drawLine(
                    color = Gray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        NavigationBar(
            containerColor = Light,
            modifier = Modifier.fillMaxSize()
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(bottom = 10.dp),
                            tint = if (currentRoute == item.route) Blue else Black
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            color = if (currentRoute == item.route) Blue else Black
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Light
                    )
                )
            }
        }
    }
}
