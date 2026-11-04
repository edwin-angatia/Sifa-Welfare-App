package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Member
import com.example.data.model.WelfareCase
import com.example.data.repository.WelfareRepository
import com.example.util.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WelfareViewModel(
    val repository: WelfareRepository
) : ViewModel() {

    val currentUserId: String = repository.getCurrentUserId() ?: ""
    val currentUserIdentifier: String = repository.getCurrentUserPhoneOrId()
    val isAdmin: Boolean get() = repository.isAdmin()

    val currentMember: StateFlow<Member?> = repository.activeMemberFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = repository.getActiveMember()
        )

    val allMembers: StateFlow<List<Member>> = repository.observeAllMembers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val userCases: StateFlow<List<WelfareCase>> = repository.observeUserCases(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allCases: StateFlow<List<WelfareCase>> = repository.observeAllCases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun saveMemberProfile(member: Member, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.saveMember(member)
            _isProcessing.value = false
            if (result.isSuccess) {
                _uiMessage.value = "Member record saved successfully!"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to save member: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun submitNewCase(welfareCase: WelfareCase, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.submitCase(welfareCase)
            _isProcessing.value = false
            if (result.isSuccess) {
                _uiMessage.value = "Welfare case application submitted!"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to submit case: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateCase(
        caseId: String,
        status: String,
        approvedAmount: Double,
        officerNotes: String,
        disbursementDate: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.updateCase(
                caseId = caseId,
                updatedStatus = status,
                approvedAmount = approvedAmount,
                officerNotes = officerNotes,
                disbursementDate = disbursementDate
            )
            _isProcessing.value = false
            if (result.isSuccess) {
                _uiMessage.value = "Case status updated to $status"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to update case: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun exportMembersExcel(context: Context) {
        viewModelScope.launch {
            try {
                val membersList = if (isAdmin) allMembers.value else listOfNotNull(currentMember.value)
                if (membersList.isEmpty()) {
                    _uiMessage.value = "No member records to export"
                    return@launch
                }
                val uri = ExportUtils.exportMembersToCsv(context, membersList)
                ExportUtils.shareReport(
                    context = context,
                    uri = uri,
                    mimeType = "text/csv",
                    title = "Export Members to Excel / CSV"
                )
                _uiMessage.value = "Members Excel/CSV export ready to download"
            } catch (e: Exception) {
                _uiMessage.value = "Export error: ${e.localizedMessage}"
            }
        }
    }

    fun exportCasesExcel(context: Context) {
        viewModelScope.launch {
            try {
                val casesList = if (isAdmin) allCases.value else userCases.value
                if (casesList.isEmpty()) {
                    _uiMessage.value = "No case records to export"
                    return@launch
                }
                val membersMap = allMembers.value.associateBy { it.id }
                val uri = ExportUtils.exportCasesToCsv(context, casesList, membersMap)
                ExportUtils.shareReport(
                    context = context,
                    uri = uri,
                    mimeType = "text/csv",
                    title = "Export Welfare Cases to Excel / CSV"
                )
                _uiMessage.value = "Cases Excel/CSV export ready to download"
            } catch (e: Exception) {
                _uiMessage.value = "Export error: ${e.localizedMessage}"
            }
        }
    }

    fun exportOfficialPdf(context: Context) {
        viewModelScope.launch {
            try {
                val membersList = if (isAdmin) allMembers.value else listOfNotNull(currentMember.value)
                val casesList = if (isAdmin) allCases.value else userCases.value
                val uri = ExportUtils.exportOfficialReportToPdf(
                    context = context,
                    members = membersList,
                    cases = casesList
                )
                ExportUtils.shareReport(
                    context = context,
                    uri = uri,
                    mimeType = "application/pdf",
                    title = "Download Welfare Official PDF Report"
                )
                _uiMessage.value = "Official PDF report generated"
            } catch (e: Exception) {
                _uiMessage.value = "PDF generation error: ${e.localizedMessage}"
            }
        }
    }

    fun recordMonthlyContribution(month: Int, onSuccess: () -> Unit = {}) {
        val memberId = currentMember.value?.id ?: currentUserId
        if (memberId.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.recordMonthlyContribution(memberId, month)
            _isProcessing.value = false
            if (result.isSuccess) {
                _uiMessage.value = "Contribution for month $month recorded successfully (Ksh 100)!"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to record contribution: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun recordMemberContribution(memberId: String, month: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.recordMonthlyContribution(memberId, month)
            _isProcessing.value = false
            if (result.isSuccess) {
                _uiMessage.value = "Contribution recorded for member!"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to record contribution: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun toggleMonthlyContribution(month: Int, onSuccess: () -> Unit = {}) {
        val memberId = currentMember.value?.id ?: currentUserId
        if (memberId.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.toggleMonthlyContribution(memberId, month)
            _isProcessing.value = false
            if (result.isSuccess) {
                val isNowPaid = result.getOrNull()?.paidMonths?.contains(month) == true
                _uiMessage.value = if (isNowPaid) "Month $month verified as paid (Ksh 100)" else "Month $month marked as unpaid"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to update contribution: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun toggleMemberContribution(memberId: String, month: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.toggleMonthlyContribution(memberId, month)
            _isProcessing.value = false
            if (result.isSuccess) {
                val isNowPaid = result.getOrNull()?.paidMonths?.contains(month) == true
                _uiMessage.value = if (isNowPaid) "Contribution marked paid (Ksh 100)" else "Contribution marked unpaid"
                onSuccess()
            } else {
                _uiMessage.value = "Failed to update contribution: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun setAdminSession(active: Boolean) {
        repository.setAdminSession(active)
    }

    fun verifyAdminPin(pin: String): Boolean {
        return repository.verifyAdminPin(pin)
    }

    fun changeAdminPin(currentPin: String, newPin: String, onSuccess: () -> Unit = {}) {
        val result = repository.changeAdminPin(currentPin, newPin)
        if (result.isSuccess) {
            _uiMessage.value = "Admin secret PIN updated successfully!"
            onSuccess()
        } else {
            _uiMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update PIN"
        }
    }

    fun getConstitutionDocTitle(): String = repository.getConstitutionDocTitle()
    fun getConstitutionDocFileName(): String = repository.getConstitutionDocFileName()
    fun getConstitutionDocDate(): String = repository.getConstitutionDocDate()
    fun getConstitutionDocUri(): String? = repository.getConstitutionDocUri()

    fun saveConstitutionPdfInfo(title: String, filename: String, date: String, uriString: String?) {
        repository.saveConstitutionPdfInfo(title, filename, date, uriString)
        _uiMessage.value = "Constitution & Rules PDF uploaded successfully!"
    }

    fun signOut(onComplete: () -> Unit = {}) {
        repository.logout()
        onComplete()
    }
}
