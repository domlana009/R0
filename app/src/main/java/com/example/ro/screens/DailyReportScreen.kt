package com.example.ro.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ModuleStop(
    val id: String = UUID.randomUUID().toString(),
    var duration: String = "",
    var nature: String = ""
)

fun formatMinutesToHoursMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0h 0m"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}h ${minutes}m"
}

@Composable
fun TotalBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF718096)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
        )
    }
}

@Composable
fun ModuleSection(
    title: String,
    stops: List<ModuleStop>,
    module: Int,
    totalDowntime: Int,
    onAddStop: () -> Unit,
    onDeleteStop: (String) -> Unit,
    onUpdateStop: (String, String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                TextButton(onClick = onAddStop) {
                    Text("+ Ajouter Arrêt")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stops list
            if (stops.isEmpty()) {
                Text(
                    "Aucun arrêt ajouté pour le $title.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stops.forEach { stop ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = stop.duration,
                                onValueChange = { onUpdateStop(stop.id, "duration", it) },
                                modifier = Modifier.weight(3f),
                                label = { Text("Durée (ex: 1h 30)") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = stop.nature,
                                onValueChange = { onUpdateStop(stop.id, "nature", it) },
                                modifier = Modifier.weight(4f),
                                label = { Text("Nature") },
                                singleLine = true
                            )
                            IconButton(
                                onClick = { onDeleteStop(stop.id) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Text(
                "Total Arrêts $title: ${formatMinutesToHoursMinutes(totalDowntime)}",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A5568)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(selectedDate: LocalDate = LocalDate.now()) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formattedDate = selectedDate.format(dateFormatter)
    
    var module1Stops by remember { mutableStateOf(listOf(ModuleStop())) }
    var module2Stops by remember { mutableStateOf(listOf(ModuleStop())) }
    
    val totalPeriodMinutes = 24 * 60
    var module1TotalDowntime by remember { mutableStateOf(0) }
    var module2TotalDowntime by remember { mutableStateOf(0) }
    var module1OperatingTime by remember { mutableStateOf(totalPeriodMinutes) }
    var module2OperatingTime by remember { mutableStateOf(totalPeriodMinutes) }

    fun parseDurationToMinutes(duration: String): Int {
        if (duration.isEmpty()) return 0
        val cleaned = duration.replace(Regex("[^0-9Hh:·\\s]"), "").trim()
        var hours = 0
        var minutes = 0
        
        val regex1 = Regex("^(?:(\\d{1,2})\\s?[Hh:·]\\s?)?(\\d{1,2})$")
        val regex2 = Regex("^(\\d{1,2})\\s?[Hh]$")
        val regex3 = Regex("^(\\d+)$")

        regex1.find(cleaned)?.let { match ->
            hours = match.groupValues[1].toIntOrNull() ?: 0
            minutes = match.groupValues[2].toIntOrNull() ?: 0
            return (hours * 60) + minutes
        }
        
        regex2.find(cleaned)?.let { match ->
            hours = match.groupValues[1].toIntOrNull() ?: 0
            return hours * 60
        }
        
        regex3.find(cleaned)?.let { match ->
            minutes = match.groupValues[1].toIntOrNull() ?: 0
            return minutes
        }
        
        return 0
    }

    fun calculateTotals(stops: List<ModuleStop>): Map<String, Int> {
        var totalDowntime = 0
        for (stop in stops) {
            totalDowntime += parseDurationToMinutes(stop.duration)
        }
        var operatingTime = totalPeriodMinutes - totalDowntime
        if (operatingTime < 0) operatingTime = 0
        return mapOf(
            "totalDowntime" to totalDowntime,
            "operatingTime" to operatingTime
        )
    }

    fun recalculateTotals() {
        val m1Totals = calculateTotals(module1Stops)
        val m2Totals = calculateTotals(module2Stops)
        module1TotalDowntime = m1Totals["totalDowntime"] ?: 0
        module2TotalDowntime = m2Totals["totalDowntime"] ?: 0
        module1OperatingTime = m1Totals["operatingTime"] ?: totalPeriodMinutes
        module2OperatingTime = m2Totals["operatingTime"] ?: totalPeriodMinutes
    }

    fun addStop(module: Int) {
        if (module == 1) {
            module1Stops = module1Stops + ModuleStop()
        } else {
            module2Stops = module2Stops + ModuleStop()
        }
        recalculateTotals()
    }

    fun deleteStop(module: Int, id: String) {
        if (module == 1) {
            module1Stops = module1Stops.filter { it.id != id }
        } else {
            module2Stops = module2Stops.filter { it.id != id }
        }
        recalculateTotals()
    }

    fun updateStop(module: Int, id: String, field: String, value: String) {
        if (module == 1) {
            module1Stops = module1Stops.map { stop ->
                if (stop.id == id) {
                    when (field) {
                        "duration" -> stop.copy(duration = value)
                        "nature" -> stop.copy(nature = value)
                        else -> stop
                    }
                } else stop
            }
        } else {
            module2Stops = module2Stops.map { stop ->
                if (stop.id == id) {
                    when (field) {
                        "duration" -> stop.copy(duration = value)
                        "nature" -> stop.copy(nature = value)
                        else -> stop
                    }
                } else stop
            }
        }
        recalculateTotals()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Rapport Journalier T SUD",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement save functionality */ }) {
                        Icon(Icons.Default.Save, contentDescription = "Sauvegarder")
                    }
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RAPPORT JOURNALIER (Activité TSUD)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    formattedDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Module 1 Section
            ModuleSection(
                title = "Module 1 - Arrêts",
                stops = module1Stops,
                module = 1,
                totalDowntime = module1TotalDowntime,
                onAddStop = { addStop(1) },
                onDeleteStop = { deleteStop(1, it) },
                onUpdateStop = { id, field, value -> updateStop(1, id, field, value) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Module 2 Section
            ModuleSection(
                title = "Module 2 - Arrêts",
                stops = module2Stops,
                module = 2,
                totalDowntime = module2TotalDowntime,
                onAddStop = { addStop(2) },
                onDeleteStop = { deleteStop(2, it) },
                onUpdateStop = { id, field, value -> updateStop(2, id, field, value) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Totals Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7FAFC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Totaux Temps de Fonctionnement (24h - Arrêts)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TotalBox(
                            label = "Module 1 Fonctionnement",
                            value = formatMinutesToHoursMinutes(module1OperatingTime),
                            modifier = Modifier.weight(1f)
                        )
                        TotalBox(
                            label = "Module 2 Fonctionnement",
                            value = formatMinutesToHoursMinutes(module2OperatingTime),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement save draft */ }
                ) {
                    Text("Enregistrer Brouillon")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { /* TODO: Implement submit */ }
                ) {
                    Text("Soumettre Rapport")
                }
            }
        }
    }
} 