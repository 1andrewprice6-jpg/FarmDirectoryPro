@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.farmdirectoryupgraded.vision.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields
import com.example.farmdirectoryupgraded.vision.ledger.CaptureRepository
import com.example.farmdirectoryupgraded.vision.parsing.ValidationGate
import androidx.compose.foundation.text.KeyboardOptions
import android.graphics.BitmapFactory
import java.io.File

private val Obsidian = Color(0xFF0B0D10)
private val Panel = Color(0xFF13161B)
private val Raise = Color(0xFF1B1F27)
private val Cyan = Color(0xFF6EE7FF)
private val Amber = Color(0xFFFFB86E)
private val Rose = Color(0xFFFF6E7F)
private val Emerald = Color(0xFF6EFFB0)
private val Muted = Color(0xFF8B94A3)
private val Bright = Color(0xFFD7DCE5)

@Composable
fun CaptureReviewScreen(
    result: CaptureRepository.Result,
    onCommit: (ParsedFields) -> Unit,
    onDiscard: () -> Unit,
) {
    var odometer by remember { mutableStateOf(result.parsed.odometerMiles?.toString() ?: "") }
    var totalCost by remember { mutableStateOf(result.parsed.totalCost?.toString() ?: "") }
    var gallons by remember { mutableStateOf(result.parsed.gallons?.toString() ?: "") }
    var ppg by remember { mutableStateOf(result.parsed.pricePerGallon?.toString() ?: "") }
    var gauge by remember { mutableStateOf(result.parsed.gaugeValue?.toString() ?: "") }
    var farmId by remember { mutableStateOf(result.parsed.farmId ?: "") }
    var date by remember { mutableStateOf(result.parsed.dateIso ?: "") }

    Column(
        Modifier.fillMaxSize().background(Obsidian).verticalScroll(rememberScrollState()),
    ) {
        DecisionBanner(result.decision)

        // Raw image preview
        val bitmap = remember(result.rawImagePath) {
            runCatching {
                val f = File(result.rawImagePath)
                BitmapFactory.decodeFile(f.absolutePath)
            }.getOrNull()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(260.dp).padding(8.dp),
            )
        }

        Surface(color = Panel, modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(10.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Parsed fields", color = Bright, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))

                Field("Odometer (miles)", odometer, { odometer = it }, numeric = true)
                Field("Total cost ($)", totalCost, { totalCost = it }, numeric = true)
                Field("Gallons", gallons, { gallons = it }, numeric = true)
                Field("Price / gal", ppg, { ppg = it }, numeric = true)
                Field("Gauge value", gauge, { gauge = it }, numeric = true)
                Field("Farm ID", farmId, { farmId = it })
                Field("Date (YYYY-MM-DD)", date, { date = it })

                if (result.parsed.metrics.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Metrics", color = Muted, fontSize = 12.sp)
                    result.parsed.metrics.forEach { (k, v) ->
                        Text("  $k: $v", color = Bright, fontSize = 13.sp)
                    }
                }

                if (result.parsed.rawText.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Raw OCR", color = Muted, fontSize = 11.sp)
                    Text(
                        result.parsed.rawText.take(500),
                        color = Muted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onDiscard,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted),
                modifier = Modifier.weight(1f),
            ) { Text("Discard") }

            Button(
                onClick = {
                    onCommit(
                        ParsedFields(
                            odometerMiles = odometer.toIntOrNull(),
                            totalCost = totalCost.toDoubleOrNull(),
                            gallons = gallons.toDoubleOrNull(),
                            pricePerGallon = ppg.toDoubleOrNull(),
                            gaugeValue = gauge.toDoubleOrNull(),
                            gaugeUnit = result.parsed.gaugeUnit,
                            farmId = farmId.takeIf { it.isNotBlank() },
                            dateIso = date.takeIf { it.isNotBlank() },
                            metrics = result.parsed.metrics,
                            rawText = result.parsed.rawText,
                            categories = result.parsed.categories,
                        ),
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Obsidian),
                modifier = Modifier.weight(1.4f),
            ) { Text("Commit to Ledger", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = Muted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Raise,
            unfocusedContainerColor = Raise,
            focusedTextColor = Bright,
            unfocusedTextColor = Bright,
            focusedIndicatorColor = Cyan,
            unfocusedIndicatorColor = Muted,
            cursorColor = Cyan,
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    )
}

@Composable
private fun DecisionBanner(decision: ValidationGate.Decision) {
    val (label, detail, bg, fg, icon) = when (decision) {
        ValidationGate.Decision.Complete ->
            Quint("Complete", "All required fields present.", Emerald.copy(alpha = 0.18f), Emerald, Icons.Default.Check)
        is ValidationGate.Decision.Incomplete ->
            Quint("Incomplete", "Missing: ${decision.missing.joinToString()}", Amber.copy(alpha = 0.18f), Amber, Icons.Default.Warning)
        is ValidationGate.Decision.Inconsistent ->
            Quint("Inconsistent", decision.issues.joinToString(" · "), Rose.copy(alpha = 0.18f), Rose, Icons.Default.Error)
        is ValidationGate.Decision.Rejected ->
            Quint("Rejected", decision.reason, Muted.copy(alpha = 0.18f), Muted, Icons.Default.Error)
    }
    Surface(color = bg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = fg)
            Spacer(Modifier.height(0.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(label, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(detail, color = fg, fontSize = 11.sp)
            }
        }
    }
}

private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
