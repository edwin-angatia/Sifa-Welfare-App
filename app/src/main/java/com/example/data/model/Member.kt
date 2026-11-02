package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar

@IgnoreExtraProperties
data class Dependant(
    val name: String = "",
    val relationship: String = "Child",
    val age: Int = 0,
    val specialNeeds: String = ""
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to name,
            "relationship" to relationship,
            "age" to age
        )
        if (specialNeeds.isNotBlank()) map["specialNeeds"] = specialNeeds
        return map
    }
}

@IgnoreExtraProperties
data class NextOfKin(
    val name: String = "",
    val relationship: String = "Spouse",
    val phone: String = "",
    val address: String = ""
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to name,
            "relationship" to relationship
        )
        if (phone.isNotBlank()) map["phone"] = phone
        if (address.isNotBlank()) map["address"] = address
        return map
    }
}

@IgnoreExtraProperties
data class Member(
    @DocumentId val id: String = "",
    val userId: String = "",
    val idNumber: String = "",
    val fullName: String = "",
    val memberNumber: String = "",
    val phone: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val category: String = "General Member",
    val status: String = "Active",
    val householdSize: Long = 1L,
    val monthlyIncome: Double = 0.0,
    val address: String = "",
    val emergencyContact: String = "",
    val notes: String = "",
    val dependants: List<Dependant> = emptyList(),
    val nextOfKin: NextOfKin? = null,
    val paidMonths: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), // 1..12 (Jan to Nov default active)
    val contributionRateKsh: Int = 100,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun getFirstName(): String {
        return fullName.trim().split("\\s+".toRegex()).firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Member"
    }

    fun getActiveUntilText(): String {
        val monthNames = listOf(
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val highestPaid = paidMonths.maxOrNull() ?: 0
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed

        return when {
            highestPaid >= currentMonth -> "Active till end of ${monthNames.getOrElse(highestPaid) { "Nov" }}"
            highestPaid > 0 -> "Active till end of ${monthNames.getOrElse(highestPaid) { "Month" }} (Arrears)"
            else -> "No contributions recorded"
        }
    }

    fun getTotalContributedKsh(): Int {
        return paidMonths.size * contributionRateKsh
    }

    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "fullName" to fullName,
            "memberNumber" to memberNumber,
            "category" to category,
            "status" to status,
            "householdSize" to householdSize,
            "monthlyIncome" to monthlyIncome,
            "paidMonths" to paidMonths,
            "contributionRateKsh" to contributionRateKsh,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (idNumber.isNotBlank()) map["idNumber"] = idNumber
        if (phone.isNotBlank()) map["phone"] = phone
        if (email.isNotBlank()) map["email"] = email
        if (passwordHash.isNotBlank()) map["passwordHash"] = passwordHash
        if (passwordSalt.isNotBlank()) map["passwordSalt"] = passwordSalt
        if (address.isNotBlank()) map["address"] = address
        if (emergencyContact.isNotBlank()) map["emergencyContact"] = emergencyContact
        if (notes.isNotBlank()) map["notes"] = notes
        if (dependants.isNotEmpty()) {
            map["dependants"] = dependants.map { it.toMap() }
        }
        if (nextOfKin != null) {
            map["nextOfKin"] = nextOfKin.toMap()
        }
        return map
    }

    fun toUpdateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "fullName" to fullName,
            "memberNumber" to memberNumber,
            "category" to category,
            "status" to status,
            "householdSize" to householdSize,
            "monthlyIncome" to monthlyIncome,
            "paidMonths" to paidMonths,
            "contributionRateKsh" to contributionRateKsh,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (idNumber.isNotBlank()) map["idNumber"] = idNumber
        if (phone.isNotBlank()) map["phone"] = phone
        if (email.isNotBlank()) map["email"] = email
        if (passwordHash.isNotBlank()) map["passwordHash"] = passwordHash
        if (passwordSalt.isNotBlank()) map["passwordSalt"] = passwordSalt
        if (address.isNotBlank()) map["address"] = address
        if (emergencyContact.isNotBlank()) map["emergencyContact"] = emergencyContact
        if (notes.isNotBlank()) map["notes"] = notes
        if (dependants.isNotEmpty()) {
            map["dependants"] = dependants.map { it.toMap() }
        }
        if (nextOfKin != null) {
            map["nextOfKin"] = nextOfKin.toMap()
        }
        return map
    }
}
