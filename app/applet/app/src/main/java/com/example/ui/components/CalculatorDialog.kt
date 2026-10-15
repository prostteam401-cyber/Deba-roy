package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyTertiary

@Composable
fun FinancialCalculatorDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Standard, 1: Loan/Kisti, 2: DPS

    val history = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("calculator_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ব্যাংক ক্যালকুলেটর (Financial Calc)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("ম্যাথ (Math)", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("কিস্তি লোন", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("DPS হিসাব", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> StandardMathCalculator(onAddToHistory = { history.add(0, it) })
                    1 -> LoanInstallmentCalculator(onAddToHistory = { history.add(0, it) })
                    2 -> DpsMaturityCalculator(onAddToHistory = { history.add(0, it) })
                }

                if (history.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "হিসাব লগ (Session History)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 70.dp)
                    ) {
                        items(history.take(4)) { entry ->
                            Text(
                                text = "• $entry",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardMathCalculator(onAddToHistory: (String) -> Unit) {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var shouldResetOnNextDigit by remember { mutableStateOf(false) }

    Column {
        // Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text(
                    text = expression.ifEmpty { " " },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = display,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { btn ->
                    val isOp = btn in listOf("÷", "×", "-", "+", "=")
                    val isAction = btn in listOf("C", "±", "%", "⌫")

                    Button(
                        onClick = {
                            when (btn) {
                                "C" -> {
                                    display = "0"
                                    expression = ""
                                    firstOperand = null
                                    pendingOp = null
                                }
                                "⌫" -> {
                                    display = if (display.length > 1) display.dropLast(1) else "0"
                                }
                                "±" -> {
                                    display = if (display.startsWith("-")) display.drop(1) else "-$display"
                                }
                                "%" -> {
                                    val num = display.toDoubleOrNull() ?: 0.0
                                    val res = num / 100.0
                                    display = res.toString()
                                }
                                in listOf("+", "-", "×", "÷") -> {
                                    firstOperand = display.toDoubleOrNull()
                                    pendingOp = btn
                                    expression = "$display $btn"
                                    shouldResetOnNextDigit = true
                                }
                                "=" -> {
                                    val second = display.toDoubleOrNull()
                                    if (firstOperand != null && pendingOp != null && second != null) {
                                        val result = when (pendingOp) {
                                            "+" -> firstOperand!! + second
                                            "-" -> firstOperand!! - second
                                            "×" -> firstOperand!! * second
                                            "÷" -> if (second != 0.0) firstOperand!! / second else Double.NaN
                                            else -> second
                                        }
                                        val resStr = if (result.isNaN()) "Error" else {
                                            if (result % 1.0 == 0.0) result.toLong().toString() else String.format("%.2f", result)
                                        }
                                        val hist = "$firstOperand $pendingOp $second = $resStr"
                                        onAddToHistory(hist)
                                        expression = hist
                                        display = resStr
                                        firstOperand = null
                                        pendingOp = null
                                        shouldResetOnNextDigit = true
                                    }
                                }
                                else -> {
                                    // Digit or .
                                    if (shouldResetOnNextDigit) {
                                        display = if (btn == ".") "0." else btn
                                        shouldResetOnNextDigit = false
                                    } else {
                                        if (display == "0" && btn != ".") {
                                            display = btn
                                        } else if (btn == "." && display.contains(".")) {
                                            // ignore second dot
                                        } else {
                                            display += btn
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                btn == "=" -> EmeraldPrimary
                                isOp -> GoldSecondary
                                isAction -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            contentColor = when {
                                btn == "=" || isOp -> Color.White
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = btn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanInstallmentCalculator(onAddToHistory: (String) -> Unit) {
    var principalStr by remember { mutableStateOf("100000") }
    var interestRateStr by remember { mutableStateOf("10") }
    var installmentsStr by remember { mutableStateOf("20") }
    var isMonthly by remember { mutableStateOf(true) }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rate = interestRateStr.toDoubleOrNull() ?: 0.0
    val installments = installmentsStr.toIntOrNull() ?: 1

    val totalInterest = principal * (rate / 100.0)
    val totalPayable = principal + totalInterest
    val perInstallment = if (installments > 0) totalPayable / installments else 0.0

    Column {
        OutlinedTextField(
            value = principalStr,
            onValueChange = { principalStr = it.filter { ch -> ch.isDigit() } },
            label = { Text("ঋণের পরিমাণ (Principal ৳)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = interestRateStr,
                onValueChange = { interestRateStr = it },
                label = { Text("সুদের হার % (Rate)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = installmentsStr,
                onValueChange = { installmentsStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("কিস্তি সংখ্যা (Count)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("কিস্তির ধরন:", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = isMonthly,
                onClick = { isMonthly = true },
                label = { Text("মাসিক (Monthly)") }
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = !isMonthly,
                onClick = { isMonthly = false },
                label = { Text("সাপ্তাহিক (Weekly)") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Breakdown Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "প্রতি কিস্তি (Per Installment): ৳${String.format("%,.2f", perInstallment)}",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "মোট পরিশোধযোগ্য (Total Payable): ৳${String.format("%,.2f", totalPayable)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "মোট সুদ/সার্ভিস চার্জ (Interest): ৳${String.format("%,.2f", totalInterest)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onAddToHistory("Loan: ৳$principal @ $rate% -> $installments x ৳${String.format("%.0f", perInstallment)}")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Text("হিসাব সংরক্ষণ (Save to Log)")
        }
    }
}

@Composable
private fun DpsMaturityCalculator(onAddToHistory: (String) -> Unit) {
    var monthlyDepositStr by remember { mutableStateOf("2000") }
    var termYearsStr by remember { mutableStateOf("3") }
    var interestRateStr by remember { mutableStateOf("9.5") }

    val monthly = monthlyDepositStr.toDoubleOrNull() ?: 0.0
    val years = termYearsStr.toIntOrNull() ?: 1
    val rate = interestRateStr.toDoubleOrNull() ?: 0.0

    val totalMonths = years * 12
    val totalPrincipal = monthly * totalMonths
    val totalInterest = totalPrincipal * (rate / 100.0) * (years / 2.0)
    val maturityAmount = totalPrincipal + totalInterest

    Column {
        OutlinedTextField(
            value = monthlyDepositStr,
            onValueChange = { monthlyDepositStr = it.filter { ch -> ch.isDigit() } },
            label = { Text("মাসিক জমা (Monthly Deposit ৳)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = termYearsStr,
                onValueChange = { termYearsStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("মেয়াদ (Years)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = interestRateStr,
                onValueChange = { interestRateStr = it },
                label = { Text("লাভের হার % (Rate)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "মেয়াদান্তে সম্ভাব্য প্রদেয়: ৳${String.format("%,.2f", maturityAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "মোট মূল জমা ($totalMonths মাস): ৳${String.format("%,.2f", totalPrincipal)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "মোট আনুমানিক মুনাফা: ৳${String.format("%,.2f", totalInterest)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onAddToHistory("DPS: ৳$monthly/mo x ${years}yrs @ $rate% -> Maturity: ৳${String.format("%.0f", maturityAmount)}")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary)
        ) {
            Text("হিসাব সংরক্ষণ (Save to Log)")
        }
    }
}
