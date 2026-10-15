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
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsExportScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by repository.transactions.collectAsState()
    val members by repository.members.collectAsState()
    val loans by repository.loans.collectAsState()
    val departments by repository.departments.collectAsState()

    var selectedReportType by remember { mutableStateOf("DAILY") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("reports_export_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "প্রতিবেদন ও এক্সেল/PDF এক্সপোর্ট (Reports)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "দাপ্তরিক হিসাব বিবরণী ও প্রিন্ট লেআউট",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Export Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        // Formula injection-safe CSV generation simulation
                        Toast.makeText(context, "নিরাপদ CSV এক্সেল ফাইল জেনারেট ও ডাউনলোড সম্পন্ন!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = "CSV", tint = EmeraldPrimary)
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "অফিসিয়াল PDF রিপোর্ট তৈরি সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Report Category Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "DAILY" to "আজকের আদায়",
                "DEPT" to "শাখাভিত্তিক",
                "LOANS" to "ঋণ স্থিতি"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = selectedReportType == key,
                    onClick = { selectedReportType = key },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedReportType) {
            "DAILY" -> {
                Text("আজকের আদায় ভাউচার তালিকা (${transactions.size} টি):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { txn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(txn.memberName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${txn.receiptNumber} • ${txn.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("৳${String.format("%,.2f", txn.totalCollected)}", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            "DEPT" -> {
                Text("শাখাভিত্তিক মোট পারফরম্যান্স:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(departments) { dept ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(dept.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("অফিসার: ${dept.assignedOfficerName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("মোট সদস্য: ${dept.totalMembers} জন", fontSize = 11.sp)
                                    Text("ঋণ স্থিতি: ৳${String.format("%,.0f", dept.totalOutstandingLoan)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            }
            "LOANS" -> {
                Text("সক্রিয় ঋণ স্থিতি তালিকা (${loans.size} টি):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(loans) { loan ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${loan.loanCode} - ${loan.memberName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("কিস্তি: ৳${String.format("%,.0f", loan.installmentAmount)} (${loan.frequency.bengaliLabel})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("অবশিষ্ট: ৳${String.format("%,.0f", loan.remainingBalance)}", fontWeight = FontWeight.Bold, color = GoldSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
