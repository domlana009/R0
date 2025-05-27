package com.example.ro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.rememberNavController
import com.example.ro.navigation.NavGraph
import com.example.ro.navigation.Screen
import com.example.ro.ui.theme.ROTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ROTheme {
                val navController = rememberNavController()
                var selectedItem by remember { mutableStateOf(0) }
                val items = listOf(
                    Triple("R0", Icons.Default.List, Screen.R0.route),
                    Triple("Camion", Icons.Default.DirectionsCar, Screen.Truck.route),
                    Triple("TNB", Icons.Default.Assessment, Screen.ActivityReport.route),
                    Triple("T SUD", Icons.Default.DateRange, Screen.DailyReport.route),
                    Triple("Rapports", Icons.Default.Visibility, Screen.ViewReports.route),
                    Triple("Profil", Icons.Default.Person, Screen.Profile.route)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White
                        ) {
                            items.forEachIndexed { index, (title, icon, route) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = title) },
                                    label = { Text(title) },
                                    selected = selectedItem == index,
                                    onClick = {
                                        selectedItem = index
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}