package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.repository.KistiBankRepository
import com.example.ui.components.FinancialCalculatorDialog
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("ড্যাশবোর্ড", Icons.Default.Dashboard),
    MEMBERS("সদস্য KYC", Icons.Default.People),
    COLLECTION("কিস্তি আদায়", Icons.Default.Payments),
    LOANS("ঋণ ব্যবস্থাপনা", Icons.Default.RequestQuote),
    SAVINGS_DPS("সঞ্চয় ও DPS", Icons.Default.Savings),
    REGULAR_PAYERS("নিয়মিত পেয়ার", Icons.Default.Verified),
    LEDGER_AUDIT("লেজার ও অডিট", Icons.Default.ReceiptLong),
    REPORTS("প্রতিবেদন", Icons.Default.Assessment),
    SETTINGS("প্রতিষ্ঠান ও টিম", Icons.Default.CorporateFare)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    repository: KistiBankRepository = remember { KistiBankRepository() }
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var selectedMemberIdForDetail by remember { mutableStateOf<String?>(null) }
    var preselectedMemberForCollection by remember { mutableStateOf<String?>(null) }
    var memberInitialCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    // Screen navigation stack handling
    BackHandler(enabled = selectedMemberIdForDetail != null || currentScreen != AppScreen.DASHBOARD) {
        if (selectedMemberIdForDetail != null) {
            selectedMemberIdForDetail = null
        } else {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    Scaffold(
        topBar = {
            if (selectedMemberIdForDetail == null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "KistiBank Financial Ltd.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "সঞ্চয়, ঋণ, ডিপিএস ও কিস্তি ব্যবস্থাপনা",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showCalculatorDialog = true },
                            modifier = Modifier.testTag("appbar_calculator_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            if (selectedMemberIdForDetail == null) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val primaryScreens = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.MEMBERS,
                        AppScreen.COLLECTION,
                        AppScreen.LOANS,
                        AppScreen.LEDGER_AUDIT
                    )

                    primaryScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedMemberIdForDetail != null) {
                MemberDetailScreen(
                    memberId = selectedMemberIdForDetail!!,
                    repository = repository,
                    onBack = { selectedMemberIdForDetail = null },
                    onNavigateToCollectionForMember = { memId ->
                        selectedMemberIdForDetail = null
                        preselectedMemberForCollection = memId
                        currentScreen = AppScreen.COLLECTION
                    }
                )
            } else {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            repository = repository,
                            onNavigateToMembers = { filter ->
                                memberInitialCategoryFilter = filter
                                currentScreen = AppScreen.MEMBERS
                            },
                            onNavigateToLoans = { currentScreen = AppScreen.LOANS },
                            onNavigateToCollection = {
                                preselectedMemberForCollection = null
                                currentScreen = AppScreen.COLLECTION
                            },
                            onNavigateToSavingsDps = { currentScreen = AppScreen.SAVINGS_DPS },
                            onNavigateToReports = { currentScreen = AppScreen.REPORTS },
                            onOpenCalculator = { showCalculatorDialog = true }
                        )
                    }
                    AppScreen.MEMBERS -> {
                        MemberKycScreen(
                            repository = repository,
                            initialFilter = memberInitialCategoryFilter,
                            onSelectMember = { member ->
                                selectedMemberIdForDetail = member.id
                            }
                        )
                    }
                    AppScreen.COLLECTION -> {
                        CollectionScreen(
                            repository = repository,
                            preselectedMemberId = preselectedMemberForCollection
                        )
                    }
                    AppScreen.LOANS -> {
                        LoanManagementScreen(repository = repository)
                    }
                    AppScreen.SAVINGS_DPS -> {
                        SavingsDpsScreen(repository = repository)
                    }
                    AppScreen.REGULAR_PAYERS -> {
                        RegularPayersScreen(repository = repository)
                    }
                    AppScreen.LEDGER_AUDIT -> {
                        LedgerAuditScreen(repository = repository)
                    }
                    AppScreen.REPORTS -> {
                        ReportsExportScreen(repository = repository)
                    }
                    AppScreen.SETTINGS -> {
                        SettingsAndTeamScreen(repository = repository)
                    }
                }
            }
        }
    }

    // Built-in Financial Calculator Modal
    if (showCalculatorDialog) {
        FinancialCalculatorDialog(onDismiss = { showCalculatorDialog = false })
    }
}
