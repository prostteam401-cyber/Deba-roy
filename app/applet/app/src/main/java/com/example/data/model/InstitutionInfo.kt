package com.example.data.model

data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val publishDate: String,
    val expiryDate: String,
    val isPublished: Boolean = true,
    val priority: String = "NORMAL" // HIGH, NORMAL
)

data class TeamMember(
    val id: String,
    val name: String,
    val role: String,
    val bengaliRole: String,
    val experience: String,
    val phone: String,
    val email: String,
    val bio: String
)

data class BankService(
    val id: String,
    val title: String,
    val bengaliTitle: String,
    val description: String,
    val iconName: String,
    val maxLimit: String,
    val interestRate: String
)

data class InstitutionSettings(
    val name: String = "KistiBank Microfinance & Savings Ltd.",
    val bengaliName: String = "কিস্তিব্যাংক ক্ষুদ্রঋণ ও সঞ্চয় সমিতি",
    val logoText: String = "KB",
    val address: String = "Level 4, Shanchay Bhaban, Motijheel C/A, Dhaka-1000",
    val phone: String = "+880 1700-123456",
    val email: String = "support@kistibank.com",
    val website: String = "https://kistibank.example.com",
    val officeHours: String = "Sat - Thu: 9:00 AM - 5:00 PM (Friday Closed)",
    val currencySymbol: String = "৳",
    val currencyCode: String = "BDT",
    val emergencyContact: String = "+880 1800-999999"
)
