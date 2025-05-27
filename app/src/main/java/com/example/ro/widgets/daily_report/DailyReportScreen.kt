package com.example.ro.widgets.daily_report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen() {
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "RAPPORT JOURNALIER",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Site: ",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF4A5568)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    var selectedSite by remember { mutableStateOf<String?>(null) }
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { },
                    ) {
                        TextField(
                            value = selectedSite ?: "",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier.menuAnchor()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Date: ",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF4A5568)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "29/04/2025",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Module 1
            ModuleSection(
                title = "Module 1",
                rows = listOf(
                    listOf("A1-20", "Marque produit...", "6H-20", "A1-40"),
                    listOf("A1-21", "Marque produit...", "6H-21", "A1-41")
                )
            )

            // Module 2
            ModuleSection(
                title = "Module 2",
                rows = listOf(
                    listOf("A2-20", "Marque produit...", "6H-20", "A2-40"),
                    listOf("A2-21", "Marque produit...", "6H-21", "A2-41")
                )
            )

            // Totals Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Totals",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2D3748)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TotalItem(label = "Module 1 HM", value = "σ-6-H-20")
                        TotalItem(label = "Module 1 HA", value = "σ-6-H-20")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TotalItem(label = "Module 2 HM", value = "σ-6-H-20")
                        TotalItem(label = "Module 2 HA", value = "σ-6-H-20")
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        "Enregistrer",
                        color = Color(0xFF48BB78)
                    )
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("Soumettre")
                }
            }
        }
    }
}

@Composable
fun ModuleSection(
    title: String,
    rows: List<List<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Surface(
                color = Color(0xFFF7FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2D3748)
                        )
                    )
                    OutlinedButton(
                        onClick = { },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4A5568)
                        )
                    ) {
                        Text("Ajouter Arrêt")
                    }
                }
            }

            // Custom Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E0))
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7FAFC))
                        .border(1.dp, Color(0xFFCBD5E0))
                ) {
                    listOf("Durée", "Nature", "HM", "HA").forEach { header ->
                        Text(
                            header,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                        )
                    }
                }

                // Data Rows
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCBD5E0))
                    ) {
                        row.forEach { cell ->
                            Text(
                                cell,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF4A5568)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TotalItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF718096)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )
        )
    }
} 