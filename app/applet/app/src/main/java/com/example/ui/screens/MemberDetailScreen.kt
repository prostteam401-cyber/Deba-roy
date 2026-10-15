package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    repository: KistiBankRepository,
    onBack: () -> Unit,
    onNavigateToCollectionForMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val loans by repository.loans.collectAsState()
    val savingsAccounts by repository.savingsAccounts.collectAsState()
    val dpsAccounts by repository.dpsAccounts.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val member = members.find { it.id == memberId }
    val memberLoan = loans.find { it.memberId == memberId && it.status != LoanStatus.CANCELLED }
    val memberSavings = savingsAccounts.find { it.memberId == memberId }
    val memberDps = dpsAccounts.find { it.memberId == memberId }

    var isNidUnmasked by remember { mutableStateOf(false) }

    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("সদস্য তথ্য পাওয়া যায়নি")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${member.fullName} (${member.memberCode})", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "${member.fullName}-এর সম্পূর্ণ প্রতিবেদন ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = EmeraldPrimary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToCollectionForMember(member.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Collect", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কিস্তি ও সঞ্চয় আদায়", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.avatarInitials.ifEmpty { member.fullName.take(2).uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.memberCode} • ${member.departmentName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "পেশা: ${member.occupation} | রক্তের গ্রুপ: ${member.bloodGroup}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Financial Summary Snapshot
            item {
                Text(
                    text = "আর্থিক বিবরণী (Financial Summary)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryBox(
                        title = "ঋণ স্থিতি",
                        amount = "৳${String.format("%,.0f", member.loanBalance)}",
                        subtext = "মোট লোন: ৳${String.format("%,.0f", member.totalLoanDisbursed)}",
                        color = if (member.loanBalance > 0) StatusLateOrange else EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBox(
                        title = "সঞ্চয় জমা",
                        amount = "৳${String.format("%,.0f", member.totalSavings)}",
                        subtext = "হিসাব: ${memberSavings?.accountNo ?: "SB-101"}",
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBox(
                        title = "DPS আমানত",
                        amount = "৳${String.format("%,.0f", member.totalDps)}",
                        subtext = "মাসিক কিস্তি জমা",
                        color = GoldSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Loan Installment Details
            if (memberLoan != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                        text = "চলতি ঋণ: ${memberLoan.loanCode}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "মঞ্জুরকৃত ঋণ: ৳${String.format("%,.0f", memberLoan.principalAmount)} (${memberLoan.frequency.bengaliLabel})",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = if (memberLoan.status == LoanStatus.COMPLETED) StatusPaidBg else EmeraldPrimaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = memberLoan.status.bengaliLabel,
                                        color = if (memberLoan.status == LoanStatus.COMPLETED) StatusPaidGreen else EmeraldOnPrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("প্রতি কিস্তির পরিমাণ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${String.format("%,.0f", memberLoan.installmentAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পরিশোধিত কিস্তি:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${member.paidInstallments} / ${member.totalInstallments} টি", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পরবর্তী কিস্তির তারিখ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(member.nextInstallmentDue, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusLateOrange)
                            }
                        }
                    }
                }
            }

            // KYC & Sensitive Identity Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "পরিচয়পত্র ও KYC তথ্য (Protected Identity)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("দলিল ধরন: ${member.kyc.docType}", fontSize = 12.sp)
                                Text(
                                    text = if (isNidUnmasked) member.kyc.docNumber else member.kyc.maskedDocNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("যাচাইকারী: ${member.kyc.verifiedBy} (${member.kyc.verifiedDate})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    isNidUnmasked = !isNidUnmasked
                                    if (isNidUnmasked) {
                                        repository.logAudit(
                                            action = "KYC_NID_UNMASKED_DETAIL",
                                            resource = "MEMBER_KYC",
                                            resourceId = member.memberCode,
                                            details = "Full NID viewed in profile by ${currentUser.fullName}"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNidUnmasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isNidUnmasked) "লুকান" else "দেখান", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Contact & Nominee Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "যোগাযোগ ও নমিনী তথ্য",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("প্রাথমিক মোবাইল: ${member.primaryMobile}", fontSize = 12.sp)
                        Text("বর্তমান ঠিকানা: ${member.presentAddress}", fontSize = 12.sp)
                        Text("স্থায়ী ঠিকানা: ${member.permanentAddress}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("নমিনী: ${member.nominee.name} (${member.nominee.relationship})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("নমিনীর মোবাইল: ${member.nominee.mobile.ifEmpty { "প্রযোজ্য নয়" }}", fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun SummaryBox(title: String, amount: String, subtext: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            Text(text = subtext, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
