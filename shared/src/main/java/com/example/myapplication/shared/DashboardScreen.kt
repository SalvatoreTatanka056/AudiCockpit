package com.example.myapplication.shared

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AudiLogo(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE2E8F0)
) {
    Canvas(modifier = modifier) {
        val ringRadius = size.height * 0.42f
        val strokeWidth = ringRadius * 0.22f
        val overlapOffset = ringRadius * 1.25f

        val centerY = size.height / 2f
        val startX = size.width / 2f - (overlapOffset * 1.5f)

        for (i in 0..3) {
            val cx = startX + i * overlapOffset
            drawCircle(
                color = color,
                radius = ringRadius,
                center = Offset(cx, centerY),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val surfaceColor = if (state.isNightMode) Color(0xFF0F121C) else Color(0xFFE2E8F0)
    val cardBgColor = if (state.isNightMode) Color(0xFF1A2130) else Color(0xFFFFFFFF)
    val textColor = if (state.isNightMode) Color.White else Color(0xFF0F172A)
    val subTextColor = if (state.isNightMode) Color(0xFF8E9AAF) else Color(0xFF475569)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopIndicatorBar(
                turnIndicator = state.turnIndicator,
                isTurnLightOn = state.isTurnLightOn,
                isNightMode = state.isNightMode,
                speedLimitKmh = state.speedLimitKmh,
                isSpeedLimitExceeded = state.isSpeedLimitExceeded,
                currentTime = state.currentTimeString,
                currentDate = state.currentDateString,
                outdoorTempC = state.outdoorTempC,
                compassHeading = state.compassHeading,
                onSelectTurn = viewModel::setTurnIndicator,
                onToggleNightMode = viewModel::toggleNightMode,
                onCycleSpeedLimit = viewModel::cycleSpeedLimit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1.3f),
                    contentAlignment = Alignment.Center
                ) {
                    SpeedometerGauge(
                        speed = state.speedKmh,
                        maxSpeed = state.maxSpeedKmh,
                        isSpeedLimitExceeded = state.isSpeedLimitExceeded,
                        isNightMode = state.isNightMode,
                        textColor = textColor
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AutomaticGearDisplay(
                        gearLabel = state.gearLabel,
                        subTextColor = subTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    TripComputerCard(
                        tripDurationSeconds = state.tripDurationSeconds,
                        avgSpeedKmh = state.averageSpeedKmh,
                        instFuelL100 = state.instantFuelL100Km,
                        avgFuelL100 = state.avgFuelL100Km,
                        estimatedRangeKm = state.estimatedRangeKm,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OdometerDisplay(
                        odometerKm = state.odometerKm,
                        tripKm = state.tripKm,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onResetTrip = viewModel::resetTrip
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CoolantTempDisplay(
                                tempC = state.coolantTempC,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BatteryVoltageDisplay(
                                voltage = state.batteryVoltage,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FuelGaugeDisplay(
                                fuelPercent = state.fuelLevelPercent,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.weight(1.3f),
                    contentAlignment = Alignment.Center
                ) {
                    TachometerGauge(
                        rpm = state.rpm,
                        maxRpm = state.maxRpm,
                        isNightMode = state.isNightMode,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TopIndicatorBar(
    turnIndicator: TurnIndicator,
    isTurnLightOn: Boolean,
    isNightMode: Boolean,
    speedLimitKmh: Float,
    isSpeedLimitExceeded: Boolean,
    currentTime: String,
    currentDate: String,
    outdoorTempC: Float,
    compassHeading: String,
    onSelectTurn: (TurnIndicator) -> Unit,
    onToggleNightMode: () -> Unit,
    onCycleSpeedLimit: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exit / Back Button + Left Turn Signal + Compass Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Exit App Button
            IconButton(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF252D3C))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit App",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            val leftColor = if (turnIndicator == TurnIndicator.LEFT || turnIndicator == TurnIndicator.HAZARD) {
                if (isTurnLightOn) Color(0xFF00FF66) else Color(0xFF1E3827)
            } else Color(0xFF1E2836)

            IconButton(
                onClick = { onSelectTurn(TurnIndicator.LEFT) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(leftColor)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Left Signal",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A2130))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Compass",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = compassHeading,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Center Audi Logo, Clock, Date, Outdoor Temp
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AudiLogo(
                    modifier = Modifier.size(width = 52.dp, height = 18.dp),
                    color = if (isNightMode) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$currentTime  |  $currentDate",
                    color = if (isNightMode) Color(0xFF8E9AAF) else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A2130))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${outdoorTempC.toInt()}°C",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSpeedLimitExceeded) Color(0xFFFF3B30) else Color.White)
                    .clickable { onCycleSpeedLimit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (speedLimitKmh > 0f) "${speedLimitKmh.toInt()}" else "OFF",
                    color = if (isSpeedLimitExceeded) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = onToggleNightMode,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isNightMode) Color(0xFF1A2130) else Color(0xFFCBD5E1))
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessMedium,
                    contentDescription = "Theme Toggle",
                    tint = if (isNightMode) Color(0xFF00E5FF) else Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Right Turn Signal & Hazard Signal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hazardColor = if (turnIndicator == TurnIndicator.HAZARD) {
                if (isTurnLightOn) Color(0xFFFF3B30) else Color(0xFF4D1410)
            } else Color(0xFF2A1E24)

            IconButton(
                onClick = { onSelectTurn(TurnIndicator.HAZARD) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(hazardColor)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Hazard Signal",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            val rightColor = if (turnIndicator == TurnIndicator.RIGHT || turnIndicator == TurnIndicator.HAZARD) {
                if (isTurnLightOn) Color(0xFF00FF66) else Color(0xFF1E3827)
            } else Color(0xFF1E2836)

            IconButton(
                onClick = { onSelectTurn(TurnIndicator.RIGHT) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(rightColor)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Right Signal",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SpeedometerGauge(
    speed: Float,
    maxSpeed: Float,
    isSpeedLimitExceeded: Boolean,
    isNightMode: Boolean,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width.coerceAtMost(size.height) / 2f - 24.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f

            val trackColor = if (isNightMode) Color(0xFF1E2838) else Color(0xFFCBD5E1)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val activeSweep = (speed / maxSpeed).coerceIn(0f, 1f) * sweepAngle
            val speedArcColor = if (isSpeedLimitExceeded) Color(0xFFFF3B30) else Color(0xFF00E5FF)
            drawArc(
                color = speedArcColor,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val numTicks = 10
            val labelColorInt = if (isNightMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            val textPaint = android.graphics.Paint().apply {
                color = labelColorInt
                textSize = 10.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            for (i in 0..numTicks) {
                val fraction = i.toFloat() / numTicks
                val angleDeg = startAngle + fraction * sweepAngle
                val angleRad = angleDeg * (PI / 180f).toFloat()

                val innerR = radius - 18.dp.toPx()
                val outerR = radius - 6.dp.toPx()

                val startPt = Offset(
                    center.x + innerR * cos(angleRad),
                    center.y + innerR * sin(angleRad)
                )
                val endPt = Offset(
                    center.x + outerR * cos(angleRad),
                    center.y + outerR * sin(angleRad)
                )

                drawLine(
                    color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    start = startPt,
                    end = endPt,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val speedValue = (fraction * maxSpeed).toInt()
                val labelRadius = radius - 32.dp.toPx()
                val labelX = center.x + labelRadius * cos(angleRad)
                val labelY = center.y + labelRadius * sin(angleRad) + (textPaint.textSize / 3f)

                drawContext.canvas.nativeCanvas.drawText(
                    "$speedValue",
                    labelX,
                    labelY,
                    textPaint
                )
            }

            val needleAngle = startAngle + (speed / maxSpeed).coerceIn(0f, 1f) * sweepAngle
            rotate(degrees = needleAngle - 270f, pivot = center) {
                val needlePath = Path().apply {
                    moveTo(center.x - 6.dp.toPx(), center.y)
                    lineTo(center.x + 6.dp.toPx(), center.y)
                    lineTo(center.x, center.y - radius + 12.dp.toPx())
                    close()
                }
                drawPath(needlePath, color = Color(0xFFFF3B30))
            }
            drawCircle(color = if (isNightMode) Color.White else Color(0xFF0F172A), radius = 10.dp.toPx(), center = center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${speed.toInt()}",
                color = if (isSpeedLimitExceeded) Color(0xFFFF3B30) else textColor,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (isSpeedLimitExceeded) "LIMIT EXCEEDED!" else "km/h",
                color = if (isSpeedLimitExceeded) Color(0xFFFF3B30) else Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TachometerGauge(
    rpm: Float,
    maxRpm: Float,
    isNightMode: Boolean,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width.coerceAtMost(size.height) / 2f - 24.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f

            val trackColor = if (isNightMode) Color(0xFF1E2838) else Color(0xFFCBD5E1)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val redlineStartFraction = 6000f / maxRpm
            drawArc(
                color = Color(0xFFFF3B30).copy(alpha = 0.5f),
                startAngle = startAngle + redlineStartFraction * sweepAngle,
                sweepAngle = (1f - redlineStartFraction) * sweepAngle,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val activeSweep = (rpm / maxRpm).coerceIn(0f, 1f) * sweepAngle
            val arcColor = if (rpm >= 6000f) Color(0xFFFF3B30) else Color(0xFF76FF03)
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val numTicks = 8
            val labelColorInt = if (isNightMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            val textPaint = android.graphics.Paint().apply {
                color = labelColorInt
                textSize = 12.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            for (i in 0..numTicks) {
                val fraction = i.toFloat() / numTicks
                val angleDeg = startAngle + fraction * sweepAngle
                val angleRad = angleDeg * (PI / 180f).toFloat()

                val innerR = radius - 18.dp.toPx()
                val outerR = radius - 6.dp.toPx()

                val startPt = Offset(
                    center.x + innerR * cos(angleRad),
                    center.y + innerR * sin(angleRad)
                )
                val endPt = Offset(
                    center.x + outerR * cos(angleRad),
                    center.y + outerR * sin(angleRad)
                )

                val tickColor = if (i >= 6) Color(0xFFFF3B30) else (if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                drawLine(
                    color = tickColor,
                    start = startPt,
                    end = endPt,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val rpmValue = (fraction * (maxRpm / 1000f)).toInt()
                val labelRadius = radius - 32.dp.toPx()
                val labelX = center.x + labelRadius * cos(angleRad)
                val labelY = center.y + labelRadius * sin(angleRad) + (textPaint.textSize / 3f)

                textPaint.color = if (rpmValue >= 6) android.graphics.Color.RED else labelColorInt
                drawContext.canvas.nativeCanvas.drawText(
                    "$rpmValue",
                    labelX,
                    labelY,
                    textPaint
                )
            }

            val needleAngle = startAngle + (rpm / maxRpm).coerceIn(0f, 1f) * sweepAngle
            rotate(degrees = needleAngle - 270f, pivot = center) {
                val needlePath = Path().apply {
                    moveTo(center.x - 6.dp.toPx(), center.y)
                    lineTo(center.x + 6.dp.toPx(), center.y)
                    lineTo(center.x, center.y - radius + 12.dp.toPx())
                    close()
                }
                drawPath(needlePath, color = Color.Yellow)
            }
            drawCircle(color = if (isNightMode) Color.White else Color(0xFF0F172A), radius = 10.dp.toPx(), center = center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(rpm / 1000f).let { String.format("%.1f", it) }}",
                color = textColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "x1000 RPM",
                color = Color(0xFF76FF03),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AutomaticGearDisplay(
    gearLabel: String,
    subTextColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF00E5FF))
                .padding(horizontal = 24.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gearLabel,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "AUTO",
            color = subTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TripComputerCard(
    tripDurationSeconds: Long,
    avgSpeedKmh: Float,
    instFuelL100: Float,
    avgFuelL100: Float,
    estimatedRangeKm: Int,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    val hours = tripDurationSeconds / 3600
    val minutes = (tripDurationSeconds % 3600) / 60
    val seconds = tripDurationSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "TIME", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = timeFormatted, color = textColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "AVG SPEED", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "${String.format("%.1f", avgSpeedKmh)} km/h", color = Color(0xFF00E5FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "CONSUMPTION", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "${String.format("%.1f", instFuelL100)} L/100km", color = Color(0xFFFF9500), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "EST RANGE", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "$estimatedRangeKm km", color = Color(0xFF00FF66), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CoolantTempDisplay(
    tempC: Float,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Temp",
                        tint = subTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "TEMP", color = subTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${tempC.toInt()}°C",
                    color = if (tempC > 105f) Color(0xFFFF3B30) else textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val progress = ((tempC - 40f) / 90f).coerceIn(0f, 1f)
            val barColor = if (tempC > 105f) Color(0xFFFF3B30) else Color(0xFF00FF66)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = barColor,
                trackColor = Color(0xFF2A364F)
            )
        }
    }
}

@Composable
fun BatteryVoltageDisplay(
    voltage: Float,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "12V Battery",
                        tint = subTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "12V", color = subTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${String.format("%.1f", voltage)}V",
                    color = if (voltage < 11.5f) Color(0xFFFF3B30) else textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val progress = ((voltage - 10f) / 6f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF2A364F)
            )
        }
    }
}

@Composable
fun OdometerDisplay(
    odometerKm: Double,
    tripKm: Double,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color,
    onResetTrip: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ODO",
                    color = subTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", odometerKm)} km",
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TRIP",
                        color = subTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESET",
                        color = Color(0xFFFF9500),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF332314))
                            .clickable { onResetTrip() }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = "${String.format("%.1f", tripKm)} km",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FuelGaugeDisplay(
    fuelPercent: Float,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Fuel",
                        tint = subTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "FUEL", color = subTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${fuelPercent.toInt()}%",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { (fuelPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF00FF66),
                trackColor = Color(0xFF2A364F)
            )
        }
    }
}
