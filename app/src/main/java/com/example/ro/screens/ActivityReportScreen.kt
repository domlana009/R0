package com.example.ro.screens

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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class Poste {
    PREMIER, DEUXIEME, TROISIEME
}

enum class Park {
    PARK1, PARK2, PARK3
}

enum class StockType {
    NORMAL, OCEANE, PB30
}

val POSTE_LABELS = mapOf(
    Poste.PREMIER to "1er",
    Poste.DEUXIEME to "2ème",
    Poste.TROISIEME to "3ème"
)

val POSTE_TIMES = mapOf(
    Poste.TROISIEME to "22:30 - 06:30",
    Poste.PREMIER to "06:30 - 14:30",
    Poste.DEUXIEME to "14:30 - 22:30"
)

val PARK_LABELS = mapOf(
    Park.PARK1 to "PARK 1",
    Park.PARK2 to "PARK 2",
    Park.PARK3 to "PARK 3"
)

val STOCK_TYPE_LABELS = mapOf(
    StockType.NORMAL to "NORMAL",
    StockType.OCEANE to "OCEANE",
    StockType.PB30 to "PB30"
)

const val MAX_HOURS_PER_POSTE = 8
const val TOTAL_PERIOD_MINUTES = 24 * 60

data class Stop(
    val id: String = UUID.randomUUID().toString(),
    var duration: String = "",
    var nature: String = ""
)

data class Counter(
    val id: String = UUID.randomUUID().toString(),
    var poste: Poste? = null,
    var start: String = "",
    var end: String = "",
    var error: String? = null
)

data class StockEntry(
    val id: String = UUID.randomUUID().toString(),
    var poste: Poste? = null,
    var park: Park? = null,
    var type: StockType? = null,
    var quantity: String = "",
    var startTime: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityReportScreen(
    selectedDate: LocalDate = LocalDate.now(),
    previousDayThirdShiftEnd: String? = null
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formattedDate = selectedDate.format(dateFormatter)
    
    var stops by remember { mutableStateOf(listOf(Stop())) }
    var vibratorCounters by remember { mutableStateOf(listOf(Counter())) }
    var liaisonCounters by remember { mutableStateOf(listOf(Counter())) }
    var stockEntries by remember { mutableStateOf(listOf(StockEntry())) }
    
    var totalDowntime by remember { mutableStateOf(0) }
    var operatingTime by remember { mutableStateOf(TOTAL_PERIOD_MINUTES) }
    var totalVibratorMinutes by remember { mutableStateOf(0) }
    var totalLiaisonMinutes by remember { mutableStateOf(0) }
    
    var hasVibratorErrors by remember { mutableStateOf(false) }
    var hasLiaisonErrors by remember { mutableStateOf(false) }
    var hasStockErrors by remember { mutableStateOf(false) }
    
    var vibratorCounterErrors by remember { mutableStateOf(mapOf<String, String>()) }
    var liaisonCounterErrors by remember { mutableStateOf(mapOf<String, String>()) }

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

    fun formatMinutesToHoursMinutes(totalMinutes: Int): String {
        if (totalMinutes <= 0) return "0h 0m"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    fun validateAndParseCounterValue(value: String): Double? {
        if (value.isEmpty()) return 0.0
        val cleaned = value.replace(Regex("[^0-9.,]"), "").replace(",", ".")
        if (cleaned.isEmpty() || cleaned == "." || cleaned == ",") return null
        return cleaned.toDoubleOrNull()
    }

    fun calculateTotalCounterMinutes(counters: List<Counter>): Int {
        val totalHours = counters.fold(0.0) { acc, counter ->
            if (counter.error != null) return@fold acc
            val startHours = validateAndParseCounterValue(counter.start)
            val endHours = validateAndParseCounterValue(counter.end)
            if (startHours != null && endHours != null && endHours >= startHours) {
                acc + (endHours - startHours)
            } else acc
        }
        return (totalHours * 60).toInt()
    }

    fun validateCounterEntry(
        counterId: String,
        counters: List<Counter>,
        type: String,
        currentStartStr: String,
        currentEndStr: String,
        currentPoste: Poste?,
        previousDayData: String?
    ): String? {
        val startVal = validateAndParseCounterValue(currentStartStr)
        val endVal = validateAndParseCounterValue(currentEndStr)
        
        if ((currentStartStr.isNotEmpty() || currentEndStr.isNotEmpty()) && currentPoste == null) {
            return "Veuillez sélectionner un poste."
        }
        
        if (currentPoste == null && currentStartStr.isEmpty() && currentEndStr.isEmpty()) {
            return null
        }
        
        if (startVal == null && currentStartStr.isNotEmpty()) return "Début invalide."
        if (endVal == null && currentEndStr.isNotEmpty()) return "Fin invalide."
        if (startVal == null || endVal == null) return null
        if (endVal < startVal) return "Fin < Début."
        
        val durationHours = endVal - startVal
        if (durationHours > MAX_HOURS_PER_POSTE) {
            return "Durée max ($MAX_HOURS_PER_POSTE h) dépassée (${String.format("%.2f", durationHours)}h)."
        }

        var expectedPreviousFinStr: String? = null
        var previousPosteName = ""
        
        when (currentPoste) {
            Poste.PREMIER -> {
                expectedPreviousFinStr = previousDayData
                previousPosteName = "3ème (veille)"
            }
            Poste.DEUXIEME -> {
                val previousCounter = counters.find { it.poste == Poste.PREMIER && it.id != counterId }
                expectedPreviousFinStr = previousCounter?.end
                previousPosteName = "1er"
            }
            Poste.TROISIEME -> {
                val previousCounter = counters.find { it.poste == Poste.DEUXIEME && it.id != counterId }
                expectedPreviousFinStr = previousCounter?.end
                previousPosteName = "2ème"
            }
            null -> {}
        }

        if (expectedPreviousFinStr != null && currentStartStr.isNotEmpty()) {
            val expectedPreviousFinParsed = validateAndParseCounterValue(expectedPreviousFinStr)
            if (expectedPreviousFinParsed != null && startVal != expectedPreviousFinParsed) {
                return "Début ($startVal) doit correspondre à Fin ($expectedPreviousFinParsed) du $previousPosteName Poste."
            }
        }
        
        return null
    }

    fun recalculateAll() {
        // Downtime and operating
        totalDowntime = stops.sumOf { parseDurationToMinutes(it.duration) }
        operatingTime = TOTAL_PERIOD_MINUTES - totalDowntime
        if (operatingTime < 0) operatingTime = 0

        // Vibrator validation
        val newVibratorErrors = mutableMapOf<String, String>()
        var vibratorValidationPassed = true
        val validVibratorCounters = vibratorCounters.filter { counter ->
            val error = validateCounterEntry(
                counter.id,
                vibratorCounters,
                "vibrator",
                counter.start,
                counter.end,
                counter.poste,
                previousDayThirdShiftEnd
            )
            if (error != null) {
                newVibratorErrors[counter.id] = error
                vibratorValidationPassed = false
                false
            } else true
        }
        vibratorCounterErrors = newVibratorErrors
        hasVibratorErrors = !vibratorValidationPassed
        totalVibratorMinutes = calculateTotalCounterMinutes(validVibratorCounters)
        if (totalVibratorMinutes > TOTAL_PERIOD_MINUTES) hasVibratorErrors = true

        // Liaison validation
        val newLiaisonErrors = mutableMapOf<String, String>()
        var liaisonValidationPassed = true
        val validLiaisonCounters = liaisonCounters.filter { counter ->
            val error = validateCounterEntry(
                counter.id,
                liaisonCounters,
                "liaison",
                counter.start,
                counter.end,
                counter.poste,
                null
            )
            if (error != null) {
                newLiaisonErrors[counter.id] = error
                liaisonValidationPassed = false
                false
            } else true
        }
        liaisonCounterErrors = newLiaisonErrors
        hasLiaisonErrors = !liaisonValidationPassed
        totalLiaisonMinutes = calculateTotalCounterMinutes(validLiaisonCounters)
        if (totalLiaisonMinutes > TOTAL_PERIOD_MINUTES) hasLiaisonErrors = true

        // Stock validation
        hasStockErrors = stockEntries.any { entry ->
            (entry.park != null || entry.type != null || entry.quantity.isNotEmpty() || entry.startTime.isNotEmpty()) &&
            entry.poste == null
        }
    }

    fun addStop() {
        stops = stops + Stop()
        recalculateAll()
    }

    fun deleteStop(id: String) {
        stops = stops.filter { it.id != id }
        recalculateAll()
    }

    fun updateStop(id: String, field: String, value: String) {
        stops = stops.map { stop ->
            if (stop.id == id) {
                when (field) {
                    "duration" -> stop.copy(duration = value)
                    "nature" -> stop.copy(nature = value)
                    else -> stop
                }
            } else stop
        }
        recalculateAll()
    }

    fun addVibratorCounter() {
        vibratorCounters = vibratorCounters + Counter()
        recalculateAll()
    }

    fun deleteVibratorCounter(id: String) {
        vibratorCounters = vibratorCounters.filter { it.id != id }
        vibratorCounterErrors = vibratorCounterErrors.filterKeys { it != id }
        recalculateAll()
    }

    fun updateVibratorCounter(id: String, field: String, value: Any?) {
        vibratorCounters = vibratorCounters.map { counter ->
            if (counter.id == id) {
                when (field) {
                    "poste" -> counter.copy(poste = value as? Poste)
                    "start" -> counter.copy(start = value as String)
                    "end" -> counter.copy(end = value as String)
                    else -> counter
                }
            } else counter
        }
        vibratorCounterErrors = vibratorCounterErrors.filterKeys { it != id }
        recalculateAll()
    }

    fun addLiaisonCounter() {
        liaisonCounters = liaisonCounters + Counter()
        recalculateAll()
    }

    fun deleteLiaisonCounter(id: String) {
        liaisonCounters = liaisonCounters.filter { it.id != id }
        liaisonCounterErrors = liaisonCounterErrors.filterKeys { it != id }
        recalculateAll()
    }

    fun updateLiaisonCounter(id: String, field: String, value: Any?) {
        liaisonCounters = liaisonCounters.map { counter ->
            if (counter.id == id) {
                when (field) {
                    "poste" -> counter.copy(poste = value as? Poste)
                    "start" -> counter.copy(start = value as String)
                    "end" -> counter.copy(end = value as String)
                    else -> counter
                }
            } else counter
        }
        liaisonCounterErrors = liaisonCounterErrors.filterKeys { it != id }
        recalculateAll()
    }

    fun addStockEntry() {
        stockEntries = stockEntries + StockEntry()
        recalculateAll()
    }

    fun deleteStockEntry(id: String) {
        stockEntries = stockEntries.filter { it.id != id }
        recalculateAll()
    }

    fun updateStockEntry(id: String, field: String, value: Any?) {
        stockEntries = stockEntries.map { entry ->
            if (entry.id == id) {
                when (field) {
                    "poste" -> entry.copy(
                        poste = value as? Poste,
                        park = null,
                        type = null,
                        quantity = "",
                        startTime = ""
                    )
                    "park" -> entry.copy(
                        park = value as? Park,
                        type = null,
                        quantity = "",
                        startTime = ""
                    )
                    "type" -> entry.copy(
                        type = value as? StockType,
                        quantity = "",
                        startTime = ""
                    )
                    "quantity" -> entry.copy(quantity = value as String)
                    "startTime" -> entry.copy(
                        startTime = value as String,
                        park = null,
                        type = null,
                        quantity = ""
                    )
                    else -> entry
                }
            } else entry
        }
        recalculateAll()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Rapport d'activité TNB",
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
                    "RAPPORT D'ACTIVITÉ TNR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    formattedDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stops Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Arrêts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(
                            onClick = { addStop() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter Arrêt")
                            }
                        }
                    }

                    if (stops.isEmpty()) {
                        Text(
                            "Aucun arrêt ajouté.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            stops.forEach { stop ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = stop.duration,
                                        onValueChange = { updateStop(stop.id, "duration", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Durée (ex: 1h 30)") },
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = stop.nature,
                                        onValueChange = { updateStop(stop.id, "nature", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Nature") },
                                        singleLine = true
                                    )
                                    IconButton(
                                        onClick = { deleteStop(stop.id) }
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

                    Text(
                        "Total Arrêts: ${formatMinutesToHoursMinutes(totalDowntime)}",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A5568)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Operating Time Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7FAFC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Temps de Fonctionnement (24h - Arrêts)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formatMinutesToHoursMinutes(operatingTime),
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Temps de Fonctionnement Estimé") },
                        enabled = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vibrator Counters Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Compteurs Vibreurs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(
                            onClick = { addVibratorCounter() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter Vibreur")
                            }
                        }
                    }

                    if (hasVibratorErrors) {
                        Text(
                            "Erreur(s) dans les compteurs vibreurs.",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Red
                            )
                        )
                    }

                    if (vibratorCounters.isEmpty()) {
                        Text(
                            "Aucun compteur vibreur ajouté.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            vibratorCounters.forEach { counter ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = counter.poste?.let { "${POSTE_LABELS[it]} Poste (${POSTE_TIMES[it]})" } ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("Poste") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                            modifier = Modifier.menuAnchor()
                                        )
                                    }

                                    OutlinedTextField(
                                        value = counter.start,
                                        onValueChange = { updateVibratorCounter(counter.id, "start", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Début (ex: 9341.0)") },
                                        isError = vibratorCounterErrors[counter.id]?.contains("Début") == true,
                                        supportingText = {
                                            vibratorCounterErrors[counter.id]?.let {
                                                Text(it)
                                            }
                                        },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = counter.end,
                                        onValueChange = { updateVibratorCounter(counter.id, "end", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Fin (ex: 9395.3)") },
                                        isError = vibratorCounterErrors[counter.id]?.contains("Fin") == true,
                                        supportingText = {
                                            vibratorCounterErrors[counter.id]?.let {
                                                Text(it)
                                            }
                                        },
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { deleteVibratorCounter(counter.id) }
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

                    Text(
                        "Durée Totale Vibreurs: ${formatMinutesToHoursMinutes(totalVibratorMinutes)}",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A5568)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Liaison Counters Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Compteurs LIAISON",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(
                            onClick = { addLiaisonCounter() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter Liaison")
                            }
                        }
                    }

                    if (hasLiaisonErrors) {
                        Text(
                            "Erreur(s) dans les compteurs liaison.",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Red
                            )
                        )
                    }

                    if (liaisonCounters.isEmpty()) {
                        Text(
                            "Aucun compteur liaison ajouté.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            liaisonCounters.forEach { counter ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = counter.poste?.let { "${POSTE_LABELS[it]} Poste (${POSTE_TIMES[it]})" } ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("Poste") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                            modifier = Modifier.menuAnchor()
                                        )
                                    }

                                    OutlinedTextField(
                                        value = counter.start,
                                        onValueChange = { updateLiaisonCounter(counter.id, "start", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Début (ex: 100.5)") },
                                        isError = liaisonCounterErrors[counter.id]?.contains("Début") == true,
                                        supportingText = {
                                            liaisonCounterErrors[counter.id]?.let {
                                                Text(it)
                                            }
                                        },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = counter.end,
                                        onValueChange = { updateLiaisonCounter(counter.id, "end", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Fin (ex: 105.75)") },
                                        isError = liaisonCounterErrors[counter.id]?.contains("Fin") == true,
                                        supportingText = {
                                            liaisonCounterErrors[counter.id]?.let {
                                                Text(it)
                                            }
                                        },
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { deleteLiaisonCounter(counter.id) }
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

                    Text(
                        "Durée Totale Liaison: ${formatMinutesToHoursMinutes(totalLiaisonMinutes)}",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A5568)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Stock",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(
                            onClick = { addStockEntry() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter Entrée Stock")
                            }
                        }
                    }

                    if (hasStockErrors) {
                        Text(
                            "Erreur(s) dans les entrées de stock. Veuillez sélectionner un poste pour chaque entrée active.",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Red
                            )
                        )
                    }

                    if (stockEntries.isEmpty()) {
                        Text(
                            "Aucune entrée de stock ajoutée.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            stockEntries.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = entry.poste?.let { POSTE_LABELS[it] } ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("Poste") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                            modifier = Modifier.menuAnchor()
                                        )
                                    }

                                    ExposedDropdownMenuBox(
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = entry.park?.let { PARK_LABELS[it] } ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("PARK") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                            modifier = Modifier.menuAnchor(),
                                            enabled = entry.poste != null
                                        )
                                    }

                                    ExposedDropdownMenuBox(
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = entry.type?.let { STOCK_TYPE_LABELS[it] } ?: "",
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("Type Produit / Info") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                            modifier = Modifier.menuAnchor(),
                                            enabled = entry.poste != null && entry.park != null
                                        )
                                    }

                                    OutlinedTextField(
                                        value = entry.quantity,
                                        onValueChange = { updateStockEntry(entry.id, "quantity", it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Quantité / Heure") },
                                        enabled = entry.type != null,
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { deleteStockEntry(entry.id) }
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
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                    onClick = { /* TODO: Implement submit */ },
                    enabled = !hasVibratorErrors && !hasLiaisonErrors && !hasStockErrors
                ) {
                    Text("Soumettre Rapport")
                }
            }
        }
    }
} 