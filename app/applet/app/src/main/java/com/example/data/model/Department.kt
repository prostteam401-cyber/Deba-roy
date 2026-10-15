package com.example.data.model

data class Department(
    val id: String,
    val code: String,
    val name: String,
    val area: String,
    val description: String,
    val assignedOfficerId: String = "",
    val assignedOfficerName: String = "",
    val totalMembers: Int = 0,
    val totalOutstandingLoan: Double = 0.0,
    val totalSavingsCollected: Double = 0.0
)
