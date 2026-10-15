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
import com.example.data.model.Member
import com.example.data.model.PayerCategory
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularPayersScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Regular Payers, 1: Overdue / Missed

    val regularMembers = members.filter { it.payerCategory == PayerCategory.REGULAR }
    val overdueMembers = members.filter { it.payerCategory == PayerCategory.OVERDUE }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("regular_payers_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "নিয়মিত বনাম বকেয়া কিস্তি তালিকা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regular Payer & Overdue Tracking System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    repository.autoDetectMissedInstallments()
                    Toast.makeText(context, "বকেয়া স্ক্যান সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Scan", tint = EmeraldPrimary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("নিয়মিত পরিশোধকারী (${regularMembers.size})") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("বকেয়া / খেলাপী (${overdueMembers.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = if (activeTab == 0) regularMembers else overdueMembers

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = if (activeTab == 0) "কোন নিয়মিত সদস্য তালিকাভুক্ত নয়" else "আলহামদুলিল্লাহ! কোন বকেয়া কিস্তি নেই",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(currentList) { mem ->
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
                                    Text(mem.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${mem.memberCode} • ${mem.departmentName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Surface(
                                    color = if (activeTab == 0) StatusPaidBg else StatusOverdueBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (activeTab == 0) "১০০% নিয়মিত" else "বকেয়া ${mem.overdueInstallments} কিস্তি",
                                        color = if (activeTab == 0) StatusPaidGreen else StatusOverdueDarkRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পরিশোধিত কিস্তি:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${mem.paidInstallments} / ${mem.totalInstallments} টি", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("অবশিষ্ট ঋণ স্থিতি:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${String.format("%,.0f", mem.loanBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (activeTab == 0) EmeraldPrimary else StatusOverdueDarkRed)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("মোবাইল নম্বর:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(mem.primaryMobile, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            if (activeTab == 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "${mem.primaryMobile} নম্বরে কিস্তির বকেয়া তাগাদা SMS পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusOverdueDarkRed),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("তাগাদা SMS পাঠান", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
