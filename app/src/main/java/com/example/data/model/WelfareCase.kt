package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WelfareCase(
    @DocumentId val id: String = "",
    val memberId: String = "",
    val userId: String = "",
    val caseNumber: String = "",
    val title: String = "",
    val category: String = "Financial Aid",
    val status: String = "Submitted",
    val priority: String = "Normal",
    val requestedAmount: Double = 0.0,
    val approvedAmount: Double = 0.0,
    val description: String = "",
    val officerNotes: String = "",
    val disbursementDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "memberId" to memberId,
            "userId" to userId,
            "caseNumber" to caseNumber,
            "title" to title,
            "category" to category,
            "status" to status,
            "priority" to priority,
            "requestedAmount" to requestedAmount,
            "approvedAmount" to approvedAmount,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (description.isNotBlank()) map["description"] = description
        if (officerNotes.isNotBlank()) map["officerNotes"] = officerNotes
        if (disbursementDate.isNotBlank()) map["disbursementDate"] = disbursementDate
        return map
    }

    fun toUpdateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "memberId" to memberId,
            "userId" to userId,
            "caseNumber" to caseNumber,
            "title" to title,
            "category" to category,
            "status" to status,
            "priority" to priority,
            "requestedAmount" to requestedAmount,
            "approvedAmount" to approvedAmount,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (description.isNotBlank()) map["description"] = description
        if (officerNotes.isNotBlank()) map["officerNotes"] = officerNotes
        if (disbursementDate.isNotBlank()) map["disbursementDate"] = disbursementDate
        return map
    }
}
