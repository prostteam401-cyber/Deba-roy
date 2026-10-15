package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InstitutionSettings
import com.example.data.model.TransactionRecord
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun ReceiptDialog(
    transaction: TransactionRecord,
    settings: InstitutionSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Bank",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = settings.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = settings.bengaliName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = settings.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "অফিসিয়াল কিস্তি ও সঞ্চয় আদায় রশিদ (OFFICIAL RECEIPT)",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Meta
                ReceiptRow("Receipt No:", transaction.receiptNumber, isBold = true)
                ReceiptRow("Txn Code:", transaction.transactionCode)
                ReceiptRow("Date & Time:", "${transaction.date} | ${transaction.time}")
                ReceiptRow("Collector:", "${transaction.employeeName} (${transaction.employeeId})")
                ReceiptRow("Member ID / Name:", "${transaction.memberName}")
                ReceiptRow("Payment Method:", transaction.paymentMethod)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Financial Breakdown
                Text(
                    text = "আদায় বিবরণী (COLLECTION BREAKDOWN)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (transaction.loanKistiAmount > 0) {
                    ReceiptRow("কিস্তি আদায় (Loan Kisti):", "৳${String.format("%,.2f", transaction.loanKistiAmount)}")
                    ReceiptRow("  - আসল (Principal Part):", "৳${String.format("%,.2f", transaction.principalApplied)}")
                    ReceiptRow("  - লাভ (Interest Part):", "৳${String.format("%,.2f", transaction.interestApplied)}")
                    ReceiptRow("নতুন ঋণ স্থিতি (New Loan Balance):", "৳${String.format("%,.2f", transaction.newLoanBalance)}")
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (transaction.savingsDeposit > 0) {
                    ReceiptRow("সঞ্চয় জমা (Savings Deposit):", "৳${String.format("%,.2f", transaction.savingsDeposit)}")
                    ReceiptRow("নতুন সঞ্চয় স্থিতি (New Savings):", "৳${String.format("%,.2f", transaction.newSavingsBalance)}")
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (transaction.dpsDeposit > 0) {
                    ReceiptRow("DPS কিস্তি জমা (DPS Deposit):", "৳${String.format("%,.2f", transaction.dpsDeposit)}")
                    ReceiptRow("মোট DPS আমানত (Total DPS):", "৳${String.format("%,.2f", transaction.newDpsBalance)}")
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট নগদ গ্রহণ (TOTAL CASH):",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "৳${String.format("%,.2f", transaction.totalCollected)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verification Stamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Collector Signature", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "✓ Digital Verified",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(text = transaction.employeeName, style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
                        ) {
                            Text(
                                text = "AUTH VERIFIED\nSEAL #KB-88",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Thermal Printer: Receipt Sent to Print Queue", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট (Print)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("সম্পন্ন (Done)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
