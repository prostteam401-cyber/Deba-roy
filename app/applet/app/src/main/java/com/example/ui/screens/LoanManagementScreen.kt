package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanManagementScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loans by repository.loans.collectAsState()
    val members by repository.members.collectAsState()

    var showCreateLoanDialog by remember { mutableStateOf(false) }
    var selectedLoanForSchedule by remember { mutableStateOf<Loan?>(null) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredLoans = loans.filter { loan ->
        when (selectedStatusFilter) {
            "ACTIVE" -> loan.status == LoanStatus.ACTIVE
            "COMPLETED" -> loan.status == LoanStatus.COMPLETED
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("loan_management_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateLoanDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Loan") },
                text = { Text("নতুন ঋণ আবেদন ও মঞ্জুর") },
                modifier = Modifier.testTag("create_loan_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ঋণ ও কিস্তি ব্যবস্থাপনা (Loans & Kisti)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "স্বয়ংক্রিয় কিস্তি সময়সূচী ও ঋণ স্থিতি ট্র্যাকিং",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = EmeraldPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${filteredLoans.size} টি ঋণ",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldOnPrimaryContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "সকল ঋণ", "ACTIVE" to "চলমান", "COMPLETED" to "পরিশোধিত").forEach { (filterKey, label) ->
                    FilterChip(
                        selected = selectedStatusFilter == filterKey,
                        onClick = { selectedStatusFilter = filterKey },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLoans, key = { it.id }) { loan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedLoanForSchedule = loan }
                            .testTag("loan_card_${loan.loanCode}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${loan.loanCode} • ${loan.memberName}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "ধরণ: ${loan.frequency.bengaliLabel} (${loan.totalInstallments} কিস্তি)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = if (loan.status == LoanStatus.COMPLETED) StatusPaidBg else EmeraldPrimaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = loan.status.bengaliLabel,
                                        color = if (loan.status == LoanStatus.COMPLETED) StatusPaidGreen else EmeraldOnPrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("মূল ঋণ:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${String.format("%,.0f", loan.principalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("প্রতি কিস্তি:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${String.format("%,.0f", loan.installmentAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("অবশিষ্ট স্থিতি:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${String.format("%,.0f", loan.remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusLateOrange)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { selectedLoanForSchedule = loan },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("কিস্তি সময়সূচী দেখুন (View Schedule) ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Schedule Dialog
    selectedLoanForSchedule?.let { loan ->
        InstallmentScheduleDialog(
            loan = loan,
            onDismiss = { selectedLoanForSchedule = null }
        )
    }

    // Create Loan Dialog
    if (showCreateLoanDialog) {
        CreateLoanDialog(
            members = members,
            onDismiss = { showCreateLoanDialog = false },
            onSubmit = { memberId, principal, rate, freq, totalInst, firstDate ->
                val res = repository.createLoan(
                    memberId = memberId,
                    principalAmount = principal,
                    interestRatePercent = rate,
                    frequency = freq,
                    totalInstallments = totalInst,
                    firstInstallmentDate = firstDate
                )
                if (res.isSuccess) {
                    showCreateLoanDialog = false
                    Toast.makeText(context, "নতুন ঋণ ও কিস্তি শিডিউল সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "ব্যর্থ", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun InstallmentScheduleDialog(
    loan: Loan,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${loan.loanCode} কিস্তি সময়সূচী",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "${loan.memberName} • মোট: ৳${String.format("%,.0f", loan.totalPayable)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Text("নং", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(30.dp))
                    Text("তারিখ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                    Text("প্রদেয়", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("স্থিতি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.1f))
                    Text("অবস্থা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(55.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(loan.installments) { inst ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${inst.installmentNo}", fontSize = 11.sp, modifier = Modifier.width(30.dp))
                            Text(inst.dueDate, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                            Text("৳${String.format("%,.0f", inst.dueAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("৳${String.format("%,.0f", inst.remainingBalanceAfter)}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.weight(1.1f))

                            val (statusBg, statusColor, statusLabel) = when (inst.status) {
                                InstallmentStatus.PAID -> Triple(StatusPaidBg, StatusPaidGreen, "পরিশোধ")
                                InstallmentStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueDarkRed, "বকেয়া")
                                InstallmentStatus.LATE -> Triple(StatusLateBg, StatusLateOrange, "লেট")
                                InstallmentStatus.PARTIAL -> Triple(StatusPartialBg, StatusPartialYellow, "আংশিক")
                                InstallmentStatus.UNPAID -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "বাকি")
                            }

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.width(55.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    color = statusColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}

@Composable
fun CreateLoanDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onSubmit: (memberId: String, principal: Double, rate: Double, freq: LoanFrequency, installments: Int, firstDate: String) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    var principalStr by remember { mutableStateOf("50000") }
    var rateStr by remember { mutableStateOf("10") }
    var installmentsStr by remember { mutableStateOf("20") }
    var isMonthly by remember { mutableStateOf(true) }
    var firstDate by remember { mutableStateOf("2026-11-01") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val totalInst = installmentsStr.toIntOrNull() ?: 1
    val totalPayable = principal + (principal * (rate / 100.0))
    val perInst = if (totalInst > 0) totalPayable / totalInst else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "নতুন ঋণ আবেদন ও মঞ্জুর ফরম",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("ঋণগ্রহীতা সদস্য নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                members.take(4).forEach { mem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMemberId = mem.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMemberId == mem.id, onClick = { selectedMemberId = mem.id })
                        Text("${mem.fullName} (${mem.memberCode})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("মঞ্জুরকৃত মূল ঋণ (Principal ৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("সার্ভিস চার্জ %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = installmentsStr,
                        onValueChange = { installmentsStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("কিস্তি সংখ্যা") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("কিস্তির ধরন:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = isMonthly, onClick = { isMonthly = true }, label = { Text("মাসিক") })
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(selected = !isMonthly, onClick = { isMonthly = false }, label = { Text("সাপ্তাহিক") })
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("মোট পরিশোধযোগ্য: ৳${String.format("%,.0f", totalPayable)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("প্রতি কিস্তির পরিমাণ: ৳${String.format("%,.2f", perInst)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSubmit(
                            selectedMemberId,
                            principal,
                            rate,
                            if (isMonthly) LoanFrequency.MONTHLY else LoanFrequency.WEEKLY,
                            totalInst,
                            firstDate
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("অনুমোদন ও শিডিউল তৈরি (Approve & Disburse)")
                }
            }
        }
    }
}
