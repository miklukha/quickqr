package com.example.quickqrapp.presentation.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.isSelected
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

sealed class BottomNavItem(val route: String, val icon: Int, val label: String) {
    object Home : BottomNavItem("home", R.drawable.home, "Головна")
    object History : BottomNavItem("history", R.drawable.history, "Історія")
    object Generate : BottomNavItem("generate", R.drawable.generate, "Генерувати")
    object Scan : BottomNavItem("scan", R.drawable.scan, "Сканувати")
}

@Composable
fun MainScreen(
    db: FirebaseFirestore,
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
        BottomNavItem.History,
        BottomNavItem.Generate,
        BottomNavItem.Scan
    )

    NavigationBar(
        containerColor = White,
        modifier = Modifier.height(70.dp),
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
                    selectedIconColor = Blue,
                    unselectedIconColor = Black,
                    selectedTextColor = Blue,
                    unselectedTextColor = Black,
                    indicatorColor = White
                )
            )
        }
    }
}
