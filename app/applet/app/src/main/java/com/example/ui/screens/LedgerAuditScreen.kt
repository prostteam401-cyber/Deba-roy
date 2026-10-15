package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.TransactionRecord
import com.example.data.model.UserRole
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerAuditScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by repository.transactions.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Financial Ledger, 1: Security Audit Log
    var selectedTxnForReversal by remember { mutableStateOf<TransactionRecord?>(null) }
    var reversalReason by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ledger_audit_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "আর্থিক লেজার ও নিরাপত্তা অডিট (Ledger & Audit)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "অপরিবর্তনীয় ডাবল-এন্ট্রি লেনদেন ও কমপ্লায়েন্স লগ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("আর্থিক লেজার (${transactions.size})") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("নিরাপত্তা অডিট লগ (${auditLogs.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // Immutable Financial Ledger
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(transactions) { txn ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                                    Text(txn.transactionCode, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("${txn.date} ${txn.time} • ${txn.memberName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Surface(
                                    color = if (txn.isReversed) StatusOverdueBg else StatusPaidBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (txn.isReversed) "রিভার্সড (Reversed)" else "৳${String.format("%,.2f", txn.totalCollected)}",
                                        color = if (txn.isReversed) StatusOverdueDarkRed else StatusPaidGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("সংগ্রহকারী: ${txn.employeeName} | পদ্ধতি: ${txn.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("বণ্টন ➔ কিস্তি: ৳${String.format("%,.0f", txn.loanKistiAmount)} | সঞ্চয়: ৳${String.format("%,.0f", txn.savingsDeposit)} | DPS: ৳${String.format("%,.0f", txn.dpsDeposit)}", fontSize = 11.sp)

                            if (txn.isReversed) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("বাতিলের কারণ: ${txn.reversalReason}", fontSize = 10.sp, color = StatusOverdueDarkRed)
                            } else if (currentUser.role == UserRole.SUPER_ADMIN || currentUser.role == UserRole.MANAGER) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(
                                        onClick = { selectedTxnForReversal = txn },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("লেনদেন রিভার্সাল (Reverse Txn)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Security Audit Log
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(auditLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.details, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ব্যবহারকারী: ${log.userName} (${log.userRole}) • IP: ${log.ipAddress}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Reversal Authorization Dialog
    selectedTxnForReversal?.let { txn ->
        AlertDialog(
            onDismissRequest = { selectedTxnForReversal = null },
            title = { Text("লেনদেন রিভার্সাল অনুমোদন", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "সতর্কতা: আর্থিক রেকর্ড সরাসরি এডিট করা নিষিদ্ধ। রিভার্সালের ফলে পূর্বের ব্যালেন্সসমূহ স্বয়ংক্রিয়ভাবে পুনরুদ্ধার হবে এবং একটি স্থায়ী অডিট ট্রেইল তৈরি হবে।",
                        fontSize = 11.sp,
                        color = StatusLateOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ভাউচার কোড: ${txn.transactionCode}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("মোট পরিমাণ: ৳${String.format("%,.2f", txn.totalCollected)}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("বাতিলকরণের সুনির্দিষ্ট কারণ লিখুন*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reversalReason.isBlank()) {
                            Toast.makeText(context, "রিভার্সালের কারণ উল্লেখ করা বাধ্যতামূলক", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = repository.reverseTransaction(txn.id, reversalReason)
                        if (res.isSuccess) {
                            selectedTxnForReversal = null
                            reversalReason = ""
                            Toast.makeText(context, "লেনদেন সফলভাবে রিভার্স করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "রিভার্সাল ব্যর্থ", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("রিভার্সাল নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxnForReversal = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
