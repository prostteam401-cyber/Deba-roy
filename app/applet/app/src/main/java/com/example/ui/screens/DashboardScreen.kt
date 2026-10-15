package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.KistiBankRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: KistiBankRepository,
    onNavigateToMembers: (filter: String?) -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToSavingsDps: () -> Unit,
    onNavigateToReports: () -> Unit,
    onOpenCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val members by repository.members.collectAsState()
    val loans by repository.loans.collectAsState()
    val savingsAccounts by repository.savingsAccounts.collectAsState()
    val dpsAccounts by repository.dpsAccounts.collectAsState()
    val transactions by repository.transactions.collectAsState()
    val notices by repository.notices.collectAsState()
    val settings by repository.settings.collectAsState()

    var showUserSwitcher by remember { mutableStateOf(false) }

    // Derived statistics
    val totalMembersCount = members.size
    val totalLoansDisbursed = loans.sumOf { it.principalAmount }
    val totalOutstandingLoan = loans.filter { it.status == LoanStatus.ACTIVE }.sumOf { it.remainingBalance }
    val totalSavingsBalance = savingsAccounts.sumOf { it.balance }
    val totalDpsBalance = dpsAccounts.sumOf { it.totalDeposited }
    val totalCollectedAllTime = transactions.filter { !it.isReversed }.sumOf { it.totalCollected }

    val todayDate = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayTransactions = transactions.filter { it.date == todayDate && !it.isReversed }
    val todayPaidCount = todayTransactions.map { it.memberId }.distinct().size
    val overdueMembersCount = members.count { it.payerCategory == PayerCategory.OVERDUE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bank Banner & Active User Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Logo",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = settings.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = settings.bengaliName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Calculator Quick Button
                        IconButton(
                            onClick = onOpenCalculator,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // User Role Chip & Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "লগ-ইন ব্যবহারকারী:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${currentUser.fullName}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ভূমিকা: ${currentUser.role.bengaliName}",
                                fontSize = 11.sp,
                                color = GoldSecondaryContainer
                            )
                        }

                        Button(
                            onClick = { showUserSwitcher = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Switch", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রোল পরিবর্তন", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Top Notice Banner
        if (notices.isNotEmpty()) {
            val topNotice = notices.first()
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = GoldSecondaryContainer.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Notice",
                            tint = GoldSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = GoldSecondary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "জরুরি নোটিশ",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = topNotice.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = topNotice.content,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Field Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToCollection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = "Collect", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কিস্তি আদায়", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigateToMembers("ALL") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Member", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন KYC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        repository.autoDetectMissedInstallments()
                        Toast.makeText(context, "বকেয়া কিস্তি স্ক্যান সম্পন্ন!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = "Scan Overdue",
                        tint = StatusOverdueDarkRed
                    )
                }
            }
        }

        // Section Title: 8 Clickable Financial Cards
        item {
            Text(
                text = "আর্থিক সূচক কার্ড (Financial Overview)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Row 1: মোট সদস্য + মোট লোন
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMetricCard(
                    bengaliTitle = "মোট সদস্য",
                    englishTitle = "Total Members",
                    primaryValue = "$totalMembersCount জন",
                    subValue = "নিয়মিত: ${members.count { it.payerCategory == PayerCategory.REGULAR }} জন",
                    icon = Icons.Default.Groups,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = EmeraldPrimary,
                    badgeText = "সক্রিয়",
                    onClick = { onNavigateToMembers(null) },
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_members"
                )

                FinancialMetricCard(
                    bengaliTitle = "মোট লোন বিতরণ",
                    englishTitle = "Total Loans",
                    primaryValue = "৳${String.format("%,.0f", totalLoansDisbursed)}",
                    subValue = "${loans.size}টি ঋণ মঞ্জুর",
                    icon = Icons.Default.RequestQuote,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = NavyTertiary,
                    badgeText = "বিতরণ",
                    badgeColor = NavyTertiary,
                    onClick = onNavigateToLoans,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_loans"
                )
            }
        }

        // Row 2: মোট সঞ্চয় + মোট DPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMetricCard(
                    bengaliTitle = "মোট সঞ্চয় জমা",
                    englishTitle = "Total Savings",
                    primaryValue = "৳${String.format("%,.0f", totalSavingsBalance)}",
                    subValue = "${savingsAccounts.size}টি সঞ্চয় হিসাব",
                    icon = Icons.Default.Savings,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = EmeraldPrimary,
                    badgeText = "তহবিল",
                    onClick = onNavigateToSavingsDps,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_savings"
                )

                FinancialMetricCard(
                    bengaliTitle = "মোট DPS আমানত",
                    englishTitle = "Total DPS",
                    primaryValue = "৳${String.format("%,.0f", totalDpsBalance)}",
                    subValue = "${dpsAccounts.size}টি ডিপিএস স্কিম",
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = GoldSecondary,
                    badgeText = "মেয়াদী",
                    badgeColor = GoldSecondary,
                    onClick = onNavigateToSavingsDps,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_dps"
                )
            }
        }

        // Row 3: আজকের কিস্তি প্রদানকারী + আজকের কিস্তি বাকি
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMetricCard(
                    bengaliTitle = "আজকের কিস্তি আদায়",
                    englishTitle = "Today Paid Kisti",
                    primaryValue = "$todayPaidCount জন",
                    subValue = "আজ আদায়: ৳${String.format("%,.0f", todayTransactions.sumOf { it.totalCollected })}",
                    icon = Icons.Default.CheckCircle,
                    containerColor = StatusPaidBg,
                    accentColor = StatusPaidGreen,
                    badgeText = "সম্পন্ন",
                    badgeColor = StatusPaidGreen,
                    onClick = onNavigateToCollection,
                    modifier = Modifier.weight(1f),
                    testTag = "card_today_paid"
                )

                FinancialMetricCard(
                    bengaliTitle = "আজকের কিস্তি বাকি",
                    englishTitle = "Overdue / Missed",
                    primaryValue = "$overdueMembersCount জন",
                    subValue = "জরুরি তাগাদা আবশ্যক",
                    icon = Icons.Default.Warning,
                    containerColor = StatusOverdueBg,
                    accentColor = StatusOverdueDarkRed,
                    badgeText = "বকেয়া",
                    badgeColor = StatusOverdueDarkRed,
                    onClick = { onNavigateToMembers("OVERDUE") },
                    modifier = Modifier.weight(1f),
                    testTag = "card_today_missed"
                )
            }
        }

        // Row 4: মোট আদায় + মোট ঋণ স্থিতি
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMetricCard(
                    bengaliTitle = "মোট মোট আদায়",
                    englishTitle = "Total Collection",
                    primaryValue = "৳${String.format("%,.0f", totalCollectedAllTime)}",
                    subValue = "${transactions.size}টি সফল ভাউচার",
                    icon = Icons.Default.Paid,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = EmeraldPrimary,
                    badgeText = "লেজার",
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f),
                    testTag = "card_total_collection"
                )

                FinancialMetricCard(
                    bengaliTitle = "মোট ঋণ স্থিতি",
                    englishTitle = "Loan Outstanding",
                    primaryValue = "৳${String.format("%,.0f", totalOutstandingLoan)}",
                    subValue = "মাঠে বকেয়া মূলধন",
                    icon = Icons.Default.MonetizationOn,
                    containerColor = MaterialTheme.colorScheme.surface,
                    accentColor = StatusLateOrange,
                    badgeText = "চলতি স্থিতি",
                    badgeColor = StatusLateOrange,
                    onClick = onNavigateToLoans,
                    modifier = Modifier.weight(1f),
                    testTag = "card_loan_outstanding"
                )
            }
        }

        // Collection Trend Chart
        item {
            CollectionTrendChart()
        }

        // Portfolio Breakdown Chart
        item {
            FinancialPortfolioBreakdown(
                totalLoan = totalOutstandingLoan,
                totalSavings = totalSavingsBalance,
                totalDps = totalDpsBalance
            )
        }

        // Spacer at bottom
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Role Switcher Modal Dialog
    if (showUserSwitcher) {
        AlertDialog(
            onDismissRequest = { showUserSwitcher = false },
            title = {
                Text(
                    text = "ব্যবহারকারী ও ভূমিকা নির্বাচন (RBAC Role Switcher)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        text = "RBAC রোল পরিবর্তনের মাধ্যমে প্রতিটি ভূমিকার প্রবেশাধিকার পরীক্ষা করুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    repository.availableUsers.forEach { user ->
                        val isSelected = user.id == currentUser.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    repository.switchUser(user)
                                    showUserSwitcher = false
                                    Toast.makeText(context, "${user.fullName} হিসেবে লগইন করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                            color = if (isSelected) EmeraldPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        repository.switchUser(user)
                                        showUserSwitcher = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "${user.role.bengaliName} • ${user.role.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserSwitcher = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
