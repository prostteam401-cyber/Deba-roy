package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.KistiBankRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberKycScreen(
    repository: KistiBankRepository,
    initialFilter: String? = null,
    onSelectMember: (Member) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val departments by repository.departments.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(initialFilter ?: "ALL") }
    var selectedDeptFilter by remember { mutableStateOf<String?>(null) }
    var showNewMemberDialog by remember { mutableStateOf(false) }

    // Masked NID visibility tracker per member
    val unmaskedNidMap = remember { mutableStateMapOf<String, Boolean>() }

    // Filter members according to permissions and search
    val filteredMembers = members.filter { member ->
        val matchesAccess = repository.canAccessDepartment(member.departmentId)
        val matchesSearch = member.fullName.contains(searchQuery, ignoreCase = true) ||
                member.memberCode.contains(searchQuery, ignoreCase = true) ||
                member.primaryMobile.contains(searchQuery) ||
                member.departmentName.contains(searchQuery, ignoreCase = true)

        val matchesCategory = when (selectedCategoryFilter) {
            "REGULAR" -> member.payerCategory == PayerCategory.REGULAR
            "OVERDUE" -> member.payerCategory == PayerCategory.OVERDUE
            "ACTIVE_LOAN" -> member.loanBalance > 0
            "COMPLETED" -> member.payerCategory == PayerCategory.COMPLETED
            else -> true
        }

        val matchesDept = selectedDeptFilter == null || member.departmentId == selectedDeptFilter

        matchesAccess && matchesSearch && matchesCategory && matchesDept
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("member_kyc_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewMemberDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add") },
                text = { Text("নতুন সদস্য নিবন্ধন (KYC)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_member_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("সদস্য খুঁজুন (Search by ID, Name, Mobile)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "সকল সদস্য",
                    "REGULAR" to "নিয়মিত পেয়ার",
                    "OVERDUE" to "বকেয়া / খেলাপী",
                    "ACTIVE_LOAN" to "চলতি লোন"
                ).forEach { (filterKey, label) ->
                    FilterChip(
                        selected = selectedCategoryFilter == filterKey,
                        onClick = { selectedCategoryFilter = filterKey },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Department Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDeptFilter == null,
                    onClick = { selectedDeptFilter = null },
                    label = { Text("সকল এরিয়া", fontSize = 11.sp) }
                )
                departments.take(3).forEach { dept ->
                    FilterChip(
                        selected = selectedDeptFilter == dept.id,
                        onClick = { selectedDeptFilter = if (selectedDeptFilter == dept.id) null else dept.id },
                        label = { Text(dept.code, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "মোট প্রাপ্ত সদস্য: ${filteredMembers.size} জন",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Member List
            if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Empty",
                            modifier = Modifier.size(54.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোন সদস্য পাওয়া যায়নি", fontWeight = FontWeight.Bold)
                        Text("অনুগ্রহ করে ফিল্টার পরিবর্তন করুন অথবা নতুন সদস্য যুক্ত করুন", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMembers, key = { it.id }) { member ->
                        val isNidUnmasked = unmaskedNidMap[member.id] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectMember(member) }
                                .testTag("member_card_${member.memberCode}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.avatarInitials.ifEmpty { member.fullName.take(2).uppercase() },
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldOnPrimaryContainer,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = member.fullName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Payer status badge
                                            val (badgeBg, badgeColor, badgeLabel) = when (member.payerCategory) {
                                                PayerCategory.REGULAR -> Triple(StatusPaidBg, StatusPaidGreen, "নিয়মিত")
                                                PayerCategory.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueDarkRed, "বকেয়া")
                                                PayerCategory.LATE -> Triple(StatusLateBg, StatusLateOrange, "বিলম্বিত")
                                                PayerCategory.COMPLETED -> Triple(EmeraldPrimaryContainer, EmeraldPrimary, "পরিশোধিত")
                                                else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "নতুন")
                                            }

                                            Surface(
                                                color = badgeBg,
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = badgeLabel,
                                                    color = badgeColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${member.memberCode} • ${member.departmentName}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Contact & Masked NID
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = member.primaryMobile, fontSize = 12.sp)
                                    }

                                    // Sensitive NID with audit-logged unmasking
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Badge, contentDescription = "NID", modifier = Modifier.size(14.dp), tint = GoldSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isNidUnmasked) member.kyc.docNumber else member.kyc.maskedDocNumber,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        IconButton(
                                            onClick = {
                                                val nextState = !isNidUnmasked
                                                unmaskedNidMap[member.id] = nextState
                                                if (nextState) {
                                                    repository.logAudit(
                                                        action = "KYC_NID_UNMASKED",
                                                        resource = "MEMBER_KYC",
                                                        resourceId = member.memberCode,
                                                        details = "Sensitive NID unmasked and viewed by ${currentUser.fullName}"
                                                    )
                                                    Toast.makeText(context, "NID দর্শন অডিট লগে রেকর্ড করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isNidUnmasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Unmask NID",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Financial Quick Summary Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("ঋণ স্থিতি", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "৳${String.format("%,.0f", member.loanBalance)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (member.loanBalance > 0) StatusLateOrange else EmeraldPrimary
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("মোট সঞ্চয়", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("৳${String.format("%,.0f", member.totalSavings)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("DPS আমানত", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("৳${String.format("%,.0f", member.totalDps)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldSecondary)
                                        }
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
    }

    // New Member Registration KYC Dialog
    if (showNewMemberDialog) {
        NewMemberKycDialog(
            departments = departments,
            onDismiss = { showNewMemberDialog = false },
            onSubmit = { fullName, father, mother, dob, gender, blood, occ, edu, pMobile, sMobile, email, presAddr, permAddr, deptId, docType, docNum, nomName, nomRel, nomMob, nomAddr ->
                val result = repository.registerMember(
                    fullName = fullName,
                    fatherName = father,
                    motherName = mother,
                    dob = dob,
                    gender = gender,
                    bloodGroup = blood,
                    occupation = occ,
                    maritalStatus = "Married",
                    education = edu,
                    primaryMobile = pMobile,
                    secondaryMobile = sMobile,
                    email = email,
                    presentAddress = presAddr,
                    permanentAddress = permAddr,
                    departmentId = deptId,
                    docType = docType,
                    docNumber = docNum,
                    nomineeName = nomName,
                    nomineeRel = nomRel,
                    nomineeMobile = nomMob,
                    nomineeAddress = nomAddr
                )

                if (result.isSuccess) {
                    showNewMemberDialog = false
                    Toast.makeText(context, "নতুন সদস্য সফলভাবে নিবন্ধিত হয়েছে!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "নিবন্ধন ব্যর্থ", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun NewMemberKycDialog(
    departments: List<Department>,
    onDismiss: () -> Unit,
    onSubmit: (
        fullName: String, father: String, mother: String, dob: String, gender: String, blood: String,
        occupation: String, education: String, pMobile: String, sMobile: String, email: String,
        presentAddr: String, permAddr: String, deptId: String, docType: String, docNum: String,
        nomineeName: String, nomineeRel: String, nomineeMobile: String, nomineeAddr: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1992-05-15") }
    var gender by remember { mutableStateOf("Male") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var occupation by remember { mutableStateOf("Shopkeeper") }
    var education by remember { mutableStateOf("SSC") }
    var primaryMobile by remember { mutableStateOf("+880 17") }
    var secondaryMobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var presentAddress by remember { mutableStateOf("") }
    var permanentAddress by remember { mutableStateOf("") }
    var selectedDeptId by remember { mutableStateOf(departments.firstOrNull()?.id ?: "") }
    var docType by remember { mutableStateOf("Smart NID") }
    var docNumber by remember { mutableStateOf("") }
    var nomineeName by remember { mutableStateOf("") }
    var nomineeRel by remember { mutableStateOf("Wife") }
    var nomineeMobile by remember { mutableStateOf("+880 18") }
    var nomineeAddr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("kyc_registration_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "নতুন সদস্য KYC ফরম",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "Member KYC Registration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Personal
                Text("১. ব্যক্তিগত তথ্য (Personal Details)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("পূর্ণ নাম (Full Name)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("পিতার নাম (Father)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = motherName,
                        onValueChange = { motherName = it },
                        label = { Text("মাতার নাম (Mother)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("পেশা (Occupation)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("রক্তের গ্রুপ (Blood)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Contact
                Text("২. যোগাযোগ ও ঠিকানা (Contact & Address)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = primaryMobile,
                    onValueChange = { primaryMobile = it },
                    label = { Text("প্রাথমিক মোবাইল (Mobile)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = presentAddress,
                    onValueChange = { presentAddress = it },
                    label = { Text("বর্তমান ঠিকানা (Present Address)*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = permanentAddress,
                    onValueChange = { permanentAddress = it },
                    label = { Text("স্থায়ী ঠিকানা (Permanent Address)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: KYC
                Text("৩. পরিচয়পত্র ও এলাকা (KYC & Department)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("স্মার্ট এনআইডি / NID নম্বর (১০ বা ১৭ ডিজিট)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("শাখা বা এলাকা নির্বাচন করুন:", fontSize = 11.sp)
                departments.forEach { dept ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDeptId = dept.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDeptId == dept.id,
                            onClick = { selectedDeptId = dept.id }
                        )
                        Text(text = dept.name, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Nominee
                Text("৪. নমিনী তথ্য (Nominee Details)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = nomineeName,
                        onValueChange = { nomineeName = it },
                        label = { Text("নমিনীর নাম*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nomineeRel,
                        onValueChange = { nomineeRel = it },
                        label = { Text("সম্পর্ক") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || primaryMobile.isBlank() || docNumber.isBlank()) {
                            // Validation message handled by parent
                        } else {
                            onSubmit(
                                fullName, fatherName, motherName, dob, gender, bloodGroup,
                                occupation, education, primaryMobile, secondaryMobile, email,
                                presentAddress, permanentAddress, selectedDeptId, docType, docNumber,
                                nomineeName, nomineeRel, nomineeMobile, nomineeAddr
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_member_kyc_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("নিবন্ধন সম্পন্ন করুন (Submit & Create Member)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
