package com.example.farmdirectoryupgraded.vision.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.ledger.CaptureEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Obsidian = Color(0xFF0B0D10)
private val Panel    = Color(0xFF13161B)
private val Raise    = Color(0xFF1B1F27)
private val Cyan     = Color(0xFF6EE7FF)
private val Amber    = Color(0xFFFFB86E)
private val Rose     = Color(0xFFFF6E7F)
private val Emerald  = Color(0xFF6EFFB0)
private val Muted    = Color(0xFF8B94A3)
private val Bright   = Color(0xFFD7DCE5)

@Composable
fun CaptureHistoryScreen(
    viewModel: CaptureHistoryViewModel = viewModel(),
    onLinkTap: (table: String, id: Long) -> Unit = { _, _ -> },
) {
    val rows by viewModel.captures.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val detail by viewModel.detail.collectAsState()

    Box(Modifier.fillMaxSize().background(Obsidian)) {
        Column(Modifier.fillMaxSize()) {
            Header(rows.size)
            ModeChips(filters.mode) { viewModel.setMode(it) }
            StatusChips(filters.status) { viewModel.setStatus(it) }

            if (rows.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("No captures match these filters.", color = Muted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(rows, key = { it.id }) { row ->
                        CaptureRow(row) { viewModel.loadDetail(row.id) }
                    }
                }
            }
        }
        detail?.let { entity ->
            CaptureDetailOverlay(
                entity = entity,
                onDismiss = { viewModel.clearDetail() },
                onDelete = { viewModel.deleteCapture(entity.id) },
                onLinkTap = onLinkTap,
            )
        }
    }
}

@Composable
private fun Header(count: Int) {
    Surface(color = Panel, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Description, null, tint = Cyan)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Capture History", color = Color.White,
                    fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text("$count captures (forensic archive)", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ModeChips(selected: CaptureMode?, onSelect: (CaptureMode?) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("ALL", fontSize = 11.sp) },
            colors = chipColors(),
        )
        CaptureMode.values().forEach { m ->
            FilterChip(
                selected = selected == m,
                onClick = { onSelect(m) },
                label = { Text(m.displayName, fontSize = 11.sp) },
                colors = chipColors(),
            )
        }
    }
}

@Composable
private fun StatusChips(selected: String?, onSelect: (String?) -> Unit) {
    val statuses = listOf(null, "COMPLETE", "INCOMPLETE", "INCONSISTENT", "REJECTED")
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        statuses.forEach { s ->
            FilterChip(
                selected = selected == s,
                onClick = { onSelect(s) },
                label = { Text(s ?: "ANY STATUS", fontSize = 11.sp) },
                colors = chipColors(),
            )
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Raise,
    selectedContainerColor = Color(0xFF2B3B50),
    labelColor = Bright,
    selectedLabelColor = Cyan,
)

@Composable
private fun CaptureRow(row: CaptureEntity, onClick: () -> Unit) {
    val (statusColor, icon) = statusColorIcon(row.status)
    Surface(
        onClick = onClick,
        color = Panel,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            val bmp = remember(row.id) {
                runCatching {
                    val f = File(row.rawImagePath)
                    if (!f.exists()) null
                    else BitmapFactory.Options().run {
                        inSampleSize = 8
                        BitmapFactory.decodeFile(f.absolutePath, this)
                    }
                }.getOrNull()
            }
            Box(
                Modifier.size(56.dp).background(Raise, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(Icons.Default.Description, null, tint = Muted)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    CaptureMode.values().firstOrNull { it.name == row.mode }?.displayName ?: row.mode,
                    color = Bright,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
                Text(
                    SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US).format(Date(row.capturedAtEpochMs)),
                    color = Muted,
                    fontSize = 11.sp,
                )
                row.farmId?.let { Text("Farm $it", color = Amber, fontSize = 11.sp) }
            }
            Icon(icon, null, tint = statusColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CaptureDetailOverlay(
    entity: CaptureEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onLinkTap: (table: String, id: Long) -> Unit,
) {
    Surface(color = Obsidian, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(color = Panel, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Bright)
                    }
                    Text(
                        "Capture #${entity.id}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(end = 4.dp),
                    ) { Text("Delete", color = Rose) }
                }
            }

            // Full image
            val bmp = remember(entity.id) {
                runCatching {
                    val f = File(entity.rawImagePath)
                    if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                }.getOrNull()
            }
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(300.dp).padding(8.dp),
                )
            } else {
                Surface(color = Raise, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                        "Raw image missing on disk: ${entity.rawImagePath}",
                        color = Rose,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Surface(color = Panel, modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(12.dp)) {
                    DetailRow("Mode",   CaptureMode.values().firstOrNull { it.name == entity.mode }?.displayName ?: entity.mode)
                    DetailRow("Status", entity.status, valueColor = statusColorIcon(entity.status).first)
                    DetailRow("Captured at",
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date(entity.capturedAtEpochMs)))
                    entity.farmId?.let { DetailRow("Farm", it) }
                    if (entity.latitude != null && entity.longitude != null) {
                        DetailRow("GPS", "%.5f, %.5f".format(entity.latitude, entity.longitude))
                    }
                    DetailRow("Synced",
                        entity.syncedAt?.let {
                            SimpleDateFormat("MMM d HH:mm", Locale.US).format(Date(it))
                        } ?: "—",
                        valueColor = if (entity.syncedAt != null) Emerald else Muted,
                    )

                    val links = parseLinks(entity.linkedEntitiesJson)
                    if (links.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Linked logs", color = Muted, fontSize = 11.sp)
                        links.forEach { (k, v) ->
                            Surface(
                                onClick = { onLinkTap(k.removeSuffix("Id"), v) },
                                color = Raise,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            ) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(k, color = Cyan, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("#$v", color = Bright, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Parsed fields (raw JSON)", color = Muted, fontSize = 11.sp)
                    Surface(color = Raise, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            entity.parsedFieldsJson,
                            color = Bright,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Bright) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.width(100.dp))
        Text(value, color = valueColor, fontSize = 13.sp)
    }
}

private fun statusColorIcon(status: String): Pair<Color, ImageVector> = when (status) {
    "COMPLETE"     -> Emerald to Icons.Default.Check
    "INCOMPLETE"   -> Amber   to Icons.Default.Warning
    "INCONSISTENT" -> Rose    to Icons.Default.Error
    "REJECTED"     -> Muted   to Icons.Default.Cancel
    else           -> Muted   to Icons.Default.Description
}

/** Parses {"fuelLogId":42,"mileageEntryId":null} into {"fuelLogId"→42}. */
private fun parseLinks(json: String): Map<String, Long> {
    val pairs = Regex(""""(\w+)"\s*:\s*(\d+)""").findAll(json)
    return pairs.associate { it.groupValues[1] to it.groupValues[2].toLong() }
}
