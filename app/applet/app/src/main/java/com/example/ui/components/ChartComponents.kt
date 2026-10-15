package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CollectionTrendChart(
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("Weekly") }

    val days = when (selectedTimeframe) {
        "Daily" -> listOf("9 AM", "11 AM", "1 PM", "3 PM", "5 PM")
        "Weekly" -> listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
        else -> listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4")
    }

    val values = when (selectedTimeframe) {
        "Daily" -> listOf(12000f, 28000f, 45000f, 62000f, 35000f)
        "Weekly" -> listOf(48000f, 72000f, 65000f, 89000f, 94000f, 68000f)
        else -> listOf(240000f, 310000f, 290000f, 380000f)
    }

    val maxValue = values.maxOrNull() ?: 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "আদায় ট্রেন্ড চার্ট (Collection Trend)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time field & digital collections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    listOf("Daily", "Weekly", "Monthly").forEach { timeframe ->
                        FilterChip(
                            selected = selectedTimeframe == timeframe,
                            onClick = { selectedTimeframe = timeframe },
                            label = { Text(timeframe, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val barWidth = size.width / (days.size * 2f)
                val spacing = size.width / days.size

                values.forEachIndexed { index, value ->
                    val barHeight = (value / maxValue) * (size.height - 20.dp.toPx())
                    val x = index * spacing + (spacing - barWidth) / 2f
                    val y = size.height - barHeight

                    // Subtle background track
                    drawRoundRect(
                        color = Color(0xFFE2EBE5),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Active collection bar
                    drawRoundRect(
                        color = EmeraldPrimary,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialPortfolioBreakdown(
    totalLoan: Double,
    totalSavings: Double,
    totalDps: Double,
    modifier: Modifier = Modifier
) {
    val total = totalLoan + totalSavings + totalDps
    val loanPct = if (total > 0) (totalLoan / total).toFloat() else 0.5f
    val savingsPct = if (total > 0) (totalSavings / total).toFloat() else 0.3f
    val dpsPct = if (total > 0) (totalDps / total).toFloat() else 0.2f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "আর্থিক পোর্টফোলিও অনুপাত (Portfolio Ratio)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(loanPct)
                        .fillMaxHeight()
                        .background(NavyTertiary)
                )
                Box(
                    modifier = Modifier
                        .weight(savingsPct)
                        .fillMaxHeight()
                        .background(EmeraldPrimary)
                )
                Box(
                    modifier = Modifier
                        .weight(dpsPct)
                        .fillMaxHeight()
                        .background(GoldSecondary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = NavyTertiary, label = "লোন বিতরণ", amount = "৳${String.format("%,.0f", totalLoan)}")
                LegendItem(color = EmeraldPrimary, label = "সঞ্চয় তহবিল", amount = "৳${String.format("%,.0f", totalSavings)}")
                LegendItem(color = GoldSecondary, label = "DPS আমানত", amount = "৳${String.format("%,.0f", totalDps)}")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = amount, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
