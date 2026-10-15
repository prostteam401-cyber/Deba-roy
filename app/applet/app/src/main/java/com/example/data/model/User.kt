package com.example.data.model

enum class UserRole(val displayName: String, val bengaliName: String, val description: String) {
    SUPER_ADMIN("Super Admin", "সুপার অ্যাডমিন", "Full system access & administration"),
    MANAGER("Bank Manager", "শাখা ব্যবস্থাপক", "Manages loans, members, approvals & staff"),
    COLLECTION_OFFICER("Collection Officer", "মাঠকর্মী", "Field collection in assigned departments"),
    ACCOUNTANT("Accountant", "হিসাবরক্ষক", "Manages accounts, vouchers & financial ledger"),
    AUDITOR("Auditor", "নিরীক্ষক", "Read-only compliance & audit trails")
}

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val mobile: String,
    val role: UserRole,
    val assignedDepartmentIds: List<String> = emptyList(),
    val avatarUrl: String = "",
    val isActive: Boolean = true
)
