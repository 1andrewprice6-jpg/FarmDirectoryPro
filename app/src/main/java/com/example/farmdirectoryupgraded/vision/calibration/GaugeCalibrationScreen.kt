package com.example.farmdirectoryupgraded.vision.calibration

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import kotlin.math.hypot

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
fun GaugeCalibrationScreen(
    viewModel: GaugeCalibrationViewModel,
    initialPhoto: Bitmap?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val s by viewModel.state.collectAsState()

    // Load initial photo once
    remember(initialPhoto) {
        if (initialPhoto != null && s.bitmap == null) {
            viewModel.loadPhotoBitmap(initialPhoto)
        }
        Unit
    }

    Column(Modifier.fillMaxSize().background(Obsidian)) {
        Surface(color = Panel, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Bright)
                }
                Text(
                    "Gauge Calibration",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    s.step.name,
                    color = Cyan,
                    fontSize = 12.sp,
                )
            }
        }

        StepPrompt(s.step)

        s.bitmap?.let { bmp ->
            CalibrationCanvas(
                bitmap = bmp,
                center = s.center,
                radiusEdge = s.radiusEdge,
                minTip = s.minNeedleTip,
                maxTip = s.maxNeedleTip,
                onTapImagePoint = { ix, iy -> viewModel.onTap(ix, iy) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }

        if (s.step == GaugeCalibrationViewModel.Step.METADATA) {
            MetadataForm(s, viewModel)
        }

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (s.step.ordinal in 1..4) {
                Button(
                    onClick = { viewModel.back() },
                    colors = ButtonDefaults.buttonColors(containerColor = Raise, contentColor = Bright),
                    modifier = Modifier.weight(1f),
                ) { Text("Undo last tap") }
            }
            if (s.step == GaugeCalibrationViewModel.Step.DONE) {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Obsidian),
                    modifier = Modifier.weight(1.4f),
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.size(6.dp))
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        s.error?.let {
            Surface(color = Rose.copy(alpha = 0.18f), modifier = Modifier.fillMaxWidth()) {
                Text(it, color = Rose, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
private fun StepPrompt(step: GaugeCalibrationViewModel.Step) {
    val text = when (step) {
        GaugeCalibrationViewModel.Step.LOAD_PHOTO -> "Load a photo of the gauge from the camera."
        GaugeCalibrationViewModel.Step.CENTER     -> "1/4 — Tap the center of the dial."
        GaugeCalibrationViewModel.Step.RADIUS     -> "2/4 — Tap the outer edge of the dial face."
        GaugeCalibrationViewModel.Step.MIN_NEEDLE -> "3/4 — Tap the needle TIP at the known minimum value."
        GaugeCalibrationViewModel.Step.MAX_NEEDLE -> "4/4 — Tap the needle TIP at the known maximum value."
        GaugeCalibrationViewModel.Step.METADATA   -> "Enter the metadata and save."
        GaugeCalibrationViewModel.Step.DONE       -> "Saved. Future captures of this gauge will read automatically."
    }
    Surface(color = Panel, modifier = Modifier.fillMaxWidth()) {
        Text(
            text,
            color = Bright,
            fontSize = 13.sp,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun CalibrationCanvas(
    bitmap: Bitmap,
    center: GaugeCalibrationViewModel.TapPoint?,
    radiusEdge: GaugeCalibrationViewModel.TapPoint?,
    minTip: GaugeCalibrationViewModel.TapPoint?,
    maxTip: GaugeCalibrationViewModel.TapPoint?,
    onTapImagePoint: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var canvasW by remember { mutableIntStateOf(0) }
    var canvasH by remember { mutableIntStateOf(0) }

    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()

    Box(
        modifier
            .aspectRatio(ratio)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .onGloballyPositioned {
                canvasW = it.size.width
                canvasH = it.size.height
            }
            .pointerInput(bitmap) {
                detectTapGestures { offset ->
                    if (canvasW == 0 || canvasH == 0) return@detectTapGestures
                    val ix = offset.x * (bitmap.width.toFloat() / canvasW)
                    val iy = offset.y * (bitmap.height.toFloat() / canvasH)
                    onTapImagePoint(ix, iy)
                }
            },
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Gauge",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            val sx = size.width / bitmap.width.toFloat()
            val sy = size.height / bitmap.height.toFloat()

            fun toView(p: GaugeCalibrationViewModel.TapPoint) = Offset(p.x * sx, p.y * sy)

            // Dial circle
            if (center != null && radiusEdge != null) {
                val c = toView(center)
                val e = toView(radiusEdge)
                val r = hypot((e.x - c.x), (e.y - c.y))
                drawCircle(color = Cyan, radius = r, center = c, style = Stroke(width = 3f))
            }
            // Needle reference lines
            if (center != null && minTip != null) {
                drawLine(Amber, toView(center), toView(minTip), strokeWidth = 4f)
            }
            if (center != null && maxTip != null) {
                drawLine(Emerald, toView(center), toView(maxTip), strokeWidth = 4f)
            }
            // Tap markers
            center?.let { drawCircle(Cyan, 10f, toView(it)); drawCircle(Color.Black, 4f, toView(it)) }
            radiusEdge?.let { drawCircle(Cyan, 10f, toView(it)); drawCircle(Color.Black, 4f, toView(it)) }
            minTip?.let { drawCircle(Amber, 10f, toView(it)); drawCircle(Color.Black, 4f, toView(it)) }
            maxTip?.let { drawCircle(Emerald, 10f, toView(it)); drawCircle(Color.Black, 4f, toView(it)) }
        }
    }
}

@Composable
private fun MetadataForm(
    s: GaugeCalibrationViewModel.State,
    viewModel: GaugeCalibrationViewModel,
) {
    Surface(color = Panel, modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(12.dp)) {
            CalField("Gauge ID (slug)", s.gaugeId, viewModel::setGaugeId)
            CalField("Gauge label",     s.gaugeLabel, viewModel::setGaugeLabel)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalField("Min value", s.minValue, viewModel::setMinValue, numeric = true, modifier = Modifier.weight(1f))
                CalField("Max value", s.maxValue, viewModel::setMaxValue, numeric = true, modifier = Modifier.weight(1f))
                CalField("Unit",      s.unit,     viewModel::setUnit, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text("Counter-clockwise sweep", color = Bright, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = s.counterclockwise,
                    onCheckedChange = viewModel::setCounterclockwise,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Cyan,
                        checkedTrackColor = Color(0xFF2B3B50),
                        uncheckedThumbColor = Muted,
                        uncheckedTrackColor = Raise,
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.save() },
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Obsidian),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Save calibration", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun CalField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    numeric: Boolean = false,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier.fillMaxWidth().padding(vertical = 3.dp),
    )
}
