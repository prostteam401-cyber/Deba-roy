package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.InstitutionSettings
import com.example.data.model.UserRole
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.EmeraldOnPrimaryContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.theme.GoldSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndTeamScreen(
    repository: KistiBankRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by repository.settings.collectAsState()
    val teamMembers by repository.teamMembers.collectAsState()
    val services by repository.services.collectAsState()
    val notices by repository.notices.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Institution & Team, 1: Services, 2: Notices
    var showEditSettingsDialog by remember { mutableStateOf(false) }
    var showAddNoticeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_team_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "প্রতিষ্ঠান, টিম ও সেটিংস (Settings & Team)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ব্যাংক প্রোফাইল, পরিচালনা পর্ষদ ও সেবাসমূহ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentUser.role == UserRole.SUPER_ADMIN || currentUser.role == UserRole.MANAGER) {
                IconButton(onClick = { showEditSettingsDialog = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Settings", tint = EmeraldPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("প্রতিষ্ঠান ও টিম") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("আমাদের সেবা") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("নোটিশ বোর্ড (${notices.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            0 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Institution Profile Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(settings.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(settings.bengaliName, fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ঠিকানা: ${settings.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("হটলাইন: ${settings.phone} | ইমেইল: ${settings.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("অফিস সময়: ${settings.officeHours}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("মুদ্রা: ${settings.currencyCode} (${settings.currencySymbol})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text("ব্যবস্থাপনা ও পরিচালনা পর্ষদ (5 Members Team):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    items(teamMembers) { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldOnPrimaryContainer,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(member.bengaliRole, fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                    Text(member.experience, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(member.bio, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(services) { srv ->
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
                                    Text(srv.bengaliTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Surface(color = EmeraldPrimaryContainer, shape = RoundedCornerShape(6.dp)) {
                                        Text(srv.interestRate, color = EmeraldOnPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(srv.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("সর্বোচ্চ সীমা: ${srv.maxLimit}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                            }
                        }
                    }
                }
            }
            2 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (currentUser.role == UserRole.SUPER_ADMIN || currentUser.role == UserRole.MANAGER) {
                        Button(
                            onClick = { showAddNoticeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.AddComment, contentDescription = "Add Notice")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন নোটিশ প্রকাশ করুন (Publish Notice)")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(notices) { notice ->
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
                                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            color = if (notice.priority == "HIGH") StatusOverdueBg else EmeraldPrimaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (notice.priority == "HIGH") "জরুরি" else "সাধারণ",
                                                color = if (notice.priority == "HIGH") StatusOverdueDarkRed else EmeraldOnPrimaryContainer,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(notice.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("প্রকাশিত: ${notice.publishDate} • মেয়াদ: ${notice.expiryDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Notice Dialog
    if (showAddNoticeDialog) {
        var noticeTitle by remember { mutableStateOf("") }
        var noticeBody by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("NORMAL") }

        AlertDialog(
            onDismissRequest = { showAddNoticeDialog = false },
            title = { Text("নতুন নোটিশ প্রকাশ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("নোটিশ শিরোনাম*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text("বিস্তারিত বিবরণ*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("অগ্রাধিকার:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = priority == "NORMAL", onClick = { priority = "NORMAL" }, label = { Text("সাধারণ") })
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(selected = priority == "HIGH", onClick = { priority = "HIGH" }, label = { Text("জরুরি") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank() && noticeBody.isNotBlank()) {
                            repository.addNotice(noticeTitle, noticeBody, priority)
                            showAddNoticeDialog = false
                            Toast.makeText(context, "নোটিশ প্রকাশিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("প্রকাশ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoticeDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Edit Settings Dialog
    if (showEditSettingsDialog) {
        var instName by remember { mutableStateOf(settings.name) }
        var instBangla by remember { mutableStateOf(settings.bengaliName) }
        var phone by remember { mutableStateOf(settings.phone) }
        var address by remember { mutableStateOf(settings.address) }

        AlertDialog(
            onDismissRequest = { showEditSettingsDialog = false },
            title = { Text("প্রতিষ্ঠান সেটিংস সম্পাদনা", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = instName, onValueChange = { instName = it }, label = { Text("Institution Name") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = instBangla, onValueChange = { instBangla = it }, label = { Text("বাংলা নাম") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("হটলাইন ফোন") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateSettings(
                            settings.copy(
                                name = instName,
                                bengaliName = instBangla,
                                phone = phone,
                                address = address
                            )
                        )
                        showEditSettingsDialog = false
                        Toast.makeText(context, "সেটিংস আপডেট সম্পন্ন!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSettingsDialog = false }) { Text("বাতিল") }
            }
        )
    }
}
