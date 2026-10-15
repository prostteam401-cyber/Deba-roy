package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.model.*
import com.example.data.repository.KistiBankRepository
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SignaturePad
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    repository: KistiBankRepository,
    preselectedMemberId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val members by repository.members.collectAsState()
    val departments by repository.departments.collectAsState()
    val settings by repository.settings.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Mobile Field Collection, 1: Excel Sheet Table

    // Permitted departments for this user
    val accessibleDepts = departments.filter { repository.canAccessDepartment(it.id) }
    var selectedDeptId by remember { mutableStateOf(accessibleDepts.firstOrNull()?.id ?: "") }

    val deptMembers = members.filter { it.departmentId == selectedDeptId }
    var selectedMemberId by remember { mutableStateOf(preselectedMemberId ?: deptMembers.firstOrNull()?.id ?: "") }

    val activeMember = members.find { it.id == selectedMemberId }

    // Inputs
    var kistiAmountStr by remember { mutableStateOf("") }
    var savingsAmountStr by remember { mutableStateOf("0") }
    var dpsAmountStr by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var collectorSignature by remember { mutableStateOf("Digital Verified") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Completed Receipt Dialog state
    var lastReceiptTransaction by remember { mutableStateOf<TransactionRecord?>(null) }

    // When member changes, autofill default installment
    LaunchedEffect(selectedMemberId) {
        if (activeMember != null && activeMember.loanBalance > 0) {
            kistiAmountStr = activeMember.nextInstallmentAmount.toInt().toString()
        } else {
            kistiAmountStr = "0"
        }
    }

    val kistiVal = kistiAmountStr.toDoubleOrNull() ?: 0.0
    val savingsVal = savingsAmountStr.toDoubleOrNull() ?: 0.0
    val dpsVal = dpsAmountStr.toDoubleOrNull() ?: 0.0
    val totalCollecting = kistiVal + savingsVal + dpsVal

    // Calculation Preview
    val prevLoan = activeMember?.loanBalance ?: 0.0
    val newLoan = Math.max(0.0, prevLoan - kistiVal)
    val prevSavings = activeMember?.totalSavings ?: 0.0
    val newSavings = prevSavings + savingsVal
    val prevDps = activeMember?.totalDps ?: 0.0
    val newDps = prevDps + dpsVal

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("collection_screen")
    ) {
        // Tab switcher: Mobile Field Collection vs Excel Table
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("ফিল্ড কিস্তি আদায় (Field Entry)") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("এক্সেল শিট ভিউ (Excel Table)") }
            )
        }

        if (activeTab == 0) {
            // Mobile Field Collection Workflow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Department selector (RBAC Enforced)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "অনুমোদিত শাখা / এলাকা (Department Scope):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            accessibleDepts.forEach { dept ->
                                FilterChip(
                                    selected = selectedDeptId == dept.id,
                                    onClick = {
                                        selectedDeptId = dept.id
                                        selectedMemberId = members.firstOrNull { it.departmentId == dept.id }?.id ?: ""
                                    },
                                    label = { Text(dept.code) }
                                )
                            }
                        }
                    }
                }

                // Member Selector
                Text(
                    text = "সদস্য নির্বাচন করুন (Select Member):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    deptMembers.forEach { mem ->
                        val isSel = mem.id == selectedMemberId
                        Card(
                            modifier = Modifier
                                .widthIn(min = 140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedMemberId = mem.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) EmeraldPrimaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary) else null
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(mem.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text(mem.memberCode, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("বকেয়া: ৳${String.format("%,.0f", mem.loanBalance)}", fontSize = 10.sp, color = StatusLateOrange)
                            }
                        }
                    }
                }

                // Member Financial Snapshot
                if (activeMember != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                                    Text(activeMember.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${activeMember.memberCode} • ${activeMember.primaryMobile}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    color = if (activeMember.loanBalance > 0) StatusLateBg else StatusPaidBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = activeMember.payerCategory.bengaliLabel,
                                        color = if (activeMember.loanBalance > 0) StatusLateOrange else StatusPaidGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("বর্তমান ঋণ স্থিতি:", fontSize = 12.sp)
                                Text("৳${String.format("%,.2f", activeMember.loanBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusLateOrange)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("বর্তমান সঞ্চয় জমা:", fontSize = 12.sp)
                                Text("৳${String.format("%,.2f", activeMember.totalSavings)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("বর্তমান DPS জমা:", fontSize = 12.sp)
                                Text("৳${String.format("%,.2f", activeMember.totalDps)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldSecondary)
                            }
                        }
                    }

                    // Input Amounts Form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "আদায় তথ্য প্রবেশ করুন (Enter Collection Amounts)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = kistiAmountStr,
                                onValueChange = { kistiAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("১. কিস্তি আদায় (Loan Kisti Amount ৳)") },
                                leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = EmeraldPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("kisti_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = savingsAmountStr,
                                onValueChange = { savingsAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("২. সঞ্চয় জমা (Savings Deposit ৳)") },
                                leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = EmeraldPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("savings_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = dpsAmountStr,
                                onValueChange = { dpsAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("৩. DPS কিস্তি জমা (DPS Deposit ৳)") },
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldSecondary) },
                                modifier = Modifier.fillMaxWidth().testTag("dps_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment Method
                            Text("পরিশোধের মাধ্যম:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("CASH" to "নগদ (Cash)", "bKash" to "বিকাশ", "Nagad" to "নগদ").forEach { (methodKey, label) ->
                                    FilterChip(
                                        selected = paymentMethod == methodKey,
                                        onClick = { paymentMethod = methodKey },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Real-time Calculation Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সার্ভার হিসাব পূর্বরূপ (Calculation Preview)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("তাত্ক্ষণিক যাচাই", fontSize = 11.sp, color = EmeraldPrimary)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("• পূর্বের ঋণ: ৳${String.format("%,.0f", prevLoan)}  -  কিস্তি: ৳${String.format("%,.0f", kistiVal)}  ➔  নতুন স্থিতি: ৳${String.format("%,.0f", newLoan)}", fontSize = 12.sp)
                            Text("• পূর্বের সঞ্চয়: ৳${String.format("%,.0f", prevSavings)}  +  জমা: ৳${String.format("%,.0f", savingsVal)}  ➔  নতুন স্থিতি: ৳${String.format("%,.0f", newSavings)}", fontSize = 12.sp)
                            Text("• পূর্বের DPS: ৳${String.format("%,.0f", prevDps)}  +  জমা: ৳${String.format("%,.0f", dpsVal)}  ➔  নতুন DPS: ৳${String.format("%,.0f", newDps)}", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "মোট নগদ গ্রহণ (Total Cash):",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "৳${String.format("%,.2f", totalCollecting)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }

                    // Signature Pad
                    SignaturePad(
                        collectorName = currentUser.fullName,
                        onSignatureConfirmed = { collectorSignature = it }
                    )

                    // Submit Action Button
                    Button(
                        onClick = {
                            if (totalCollecting <= 0.0) {
                                Toast.makeText(context, "আদায় পরিমাণ শূন্য হতে পারে না", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitting = true
                            val idempotencyKey = UUID.randomUUID().toString()

                            val result = repository.recordCollection(
                                memberId = activeMember.id,
                                loanKistiAmount = kistiVal,
                                savingsDeposit = savingsVal,
                                dpsDeposit = dpsVal,
                                paymentMethod = paymentMethod,
                                collectorSignature = collectorSignature,
                                idempotencyKey = idempotencyKey
                            )

                            isSubmitting = false

                            if (result.isSuccess) {
                                lastReceiptTransaction = result.getOrNull()
                                Toast.makeText(context, "আদায় সফল ও লেজারে লিপিবদ্ধ হয়েছে!", Toast.LENGTH_SHORT).show()
                                // Reset inputs
                                kistiAmountStr = "0"
                                savingsAmountStr = "0"
                                dpsAmountStr = "0"
                            } else {
                                Toast.makeText(context, result.exceptionOrNull()?.message ?: "আদায় ব্যর্থ", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_collection_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Submit")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নিশ্চিত করুন ও রশিদ তৈরি করুন (Confirm & Commit)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        } else {
            // Excel-like Collection Sheet Table
            ExcelCollectionTable(
                members = deptMembers,
                onSelectMemberForField = { mem ->
                    selectedMemberId = mem.id
                    activeTab = 0
                }
            )
        }
    }

    // Official Receipt Dialog
    lastReceiptTransaction?.let { txn ->
        ReceiptDialog(
            transaction = txn,
            settings = settings,
            onDismiss = { lastReceiptTransaction = null }
        )
    }
}

@Composable
fun ExcelCollectionTable(
    members: List<Member>,
    onSelectMemberForField: (Member) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "এক্সেল-সদৃশ ফিল্ড কালেকশন শিট (Excel Collection Sheet)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "একটি ক্লিকে সদস্য নির্বাচন করে তাত্ক্ষণিক আদায় সম্পাদন করুন",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Spreadsheet View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("কোড", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp))
                    Text("নাম (Name)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                    Text("ঋণ স্থিতি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                    Text("সঞ্চয়", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("স্ট্যাটাস", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                    Text("অ্যাকশন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(55.dp))
                }
            }

            items(members) { mem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMemberForField(mem) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mem.memberCode, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                    Text(mem.fullName, fontSize = 11.sp, modifier = Modifier.weight(1.5f), maxLines = 1)
                    Text("৳${String.format("%,.0f", mem.loanBalance)}", fontSize = 11.sp, color = StatusLateOrange, modifier = Modifier.weight(1.2f))
                    Text("৳${String.format("%,.0f", mem.totalSavings)}", fontSize = 11.sp, color = EmeraldPrimary, modifier = Modifier.weight(1f))

                    val statusColor = when (mem.payerCategory) {
                        PayerCategory.REGULAR -> StatusPaidGreen
                        PayerCategory.OVERDUE -> StatusOverdueDarkRed
                        else -> Color.Gray
                    }
                    Text(
                        text = when (mem.payerCategory) {
                            PayerCategory.REGULAR -> "নিয়মিত"
                            PayerCategory.OVERDUE -> "খেলাপী"
                            else -> "অন্যান্য"
                        },
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )

                    Button(
                        onClick = { onSelectMemberForField(mem) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .width(55.dp)
                            .height(28.dp)
                    ) {
                        Text("আদায়", fontSize = 10.sp)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}
