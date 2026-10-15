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
import com.example.data.model.DpsAccount
import com.example.data.model.SavingsAccount
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsDpsScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savings by repository.savingsAccounts.collectAsState()
    val dpsAccounts by repository.dpsAccounts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Savings, 1: DPS

    val totalSavings = savings.sumOf { it.balance }
    val totalDps = dpsAccounts.sumOf { it.totalDeposited }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("savings_dps_screen")
    ) {
        // Header Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (selectedTab == 0) EmeraldPrimary else GoldSecondary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (selectedTab == 0) "মোট সঞ্চয় তহবিল স্থিতি (Total Savings)" else "মোট DPS আমানত স্থিতি (Total DPS)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
                Text(
                    text = "৳${String.format("%,.2f", if (selectedTab == 0) totalSavings else totalDps)}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = if (selectedTab == 0) "${savings.size}টি সক্রিয় সাধারণ সঞ্চয় হিসাব" else "${dpsAccounts.size}টি সক্রিয় মাসিক DPS স্কিম",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("সাধারণ সঞ্চয় (Savings)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("মাসিক DPS স্কিম") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Savings Accounts List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(savings) { acc ->
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
                                    Text(acc.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("হিসাব নং: ${acc.accountNo} • খোলা হয়েছে: ${acc.openedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "৳${String.format("%,.0f", acc.balance)}",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("মোট জমা: ৳${String.format("%,.0f", acc.totalDeposited)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("মুনাফা অর্জিত: ৳${String.format("%,.0f", acc.interestEarned)}", fontSize = 11.sp, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        } else {
            // DPS Accounts List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(dpsAccounts) { dps ->
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
                                    Text(dps.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("DPS কোড: ${dps.dpsCode} • মেয়াদ: ${dps.termMonths} মাস", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(color = GoldSecondaryContainer, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "মাসিক ৳${String.format("%,.0f", dps.monthlyInstallment)}",
                                        color = GoldOnSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("জমা হয়েছে (${dps.totalPaidMonths} মাস):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${String.format("%,.0f", dps.totalDeposited)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("মেয়াদান্তে প্রদেয়:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("৳${String.format("%,.0f", dps.expectedMaturityAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
