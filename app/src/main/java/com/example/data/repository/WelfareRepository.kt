package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.local.MemberAccountStore
import com.example.data.model.Dependant
import com.example.data.model.Member
import com.example.data.model.NextOfKin
import com.example.data.model.WelfareCase
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class WelfareRepository(
    private val db: FirebaseFirestore,
    context: Context
) {
    private val store = MemberAccountStore(context)
    private val auth = Firebase.auth

    private val _membersFlow = MutableStateFlow(store.getAllMembers())
    private val _casesFlow = MutableStateFlow(store.getAllCases())
    private val _activeMemberFlow = MutableStateFlow(store.getActiveMember())

    val activeMemberFlow: Flow<Member?> = _activeMemberFlow.asStateFlow()

    constructor(context: Context) : this(
        try {
            FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        },
        context.applicationContext
    )

    fun getActiveMember(): Member? = _activeMemberFlow.value

    fun getCurrentUserId(): String? = _activeMemberFlow.value?.id ?: auth.currentUser?.uid

    fun getCurrentUserPhoneOrId(): String = _activeMemberFlow.value?.idNumber
        ?: _activeMemberFlow.value?.phone
        ?: auth.currentUser?.email
        ?: ""

    private var adminSessionActive: Boolean = false

    fun setAdminSession(active: Boolean) {
        adminSessionActive = active
    }

    fun isAdmin(): Boolean {
        if (adminSessionActive) return true
        val email = auth.currentUser?.email
        return email != null && email.equals("eangatia@gmail.com", ignoreCase = true)
    }

    fun verifyAdminPin(enteredPin: String): Boolean {
        return store.verifyAdminPin(enteredPin)
    }

    fun changeAdminPin(currentPin: String, newPin: String): Result<Unit> {
        if (!verifyAdminPin(currentPin)) {
            return Result.failure(IllegalArgumentException("Current PIN is incorrect."))
        }
        if (newPin.trim().length < 4) {
            return Result.failure(IllegalArgumentException("New PIN must be at least 4 digits."))
        }
        val success = store.setAdminPin(newPin)
        return if (success) Result.success(Unit) else Result.failure(IllegalStateException("Could not update PIN"))
    }

    fun getConstitutionDocTitle(): String = store.getConstitutionDocTitle()
    fun getConstitutionDocFileName(): String = store.getConstitutionDocFileName()
    fun getConstitutionDocDate(): String = store.getConstitutionDocDate()
    fun getConstitutionDocUri(): String? = store.getConstitutionDocUri()

    fun saveConstitutionPdfInfo(title: String, filename: String, date: String, uriString: String?) {
        store.saveConstitutionPdfInfo(title, filename, date, uriString)
    }

    // Number-based login (ID number OR Phone number) + Password
    fun loginWithNumber(identifier: String, password: String): Result<Member> {
        val member = store.authenticateMember(identifier, password)
        return if (member != null) {
            store.setActiveMember(member)
            _activeMemberFlow.value = member
            Result.success(member)
        } else {
            Result.failure(IllegalArgumentException("Invalid ID / Phone number or incorrect password."))
        }
    }

    // Two-step sign-up: ID, Phone, Names, Password, Dependants, Next of Kin
    fun registerMemberWithDetails(
        idNumber: String,
        phone: String,
        fullName: String,
        password: String,
        dependants: List<Dependant>,
        nextOfKin: NextOfKin?,
        category: String,
        address: String
    ): Result<Member> {
        val result = store.registerMember(
            idNumber = idNumber,
            phone = phone,
            fullName = fullName,
            password = password,
            dependants = dependants,
            nextOfKin = nextOfKin,
            category = category,
            address = address
        )
        if (result.isSuccess) {
            val member = result.getOrThrow()
            _activeMemberFlow.value = member
            _membersFlow.value = store.getAllMembers()

            // Synchronize with Cloud Firestore if user has an auth token
            if (auth.currentUser != null) {
                try {
                    db.collection("members").document(member.id).set(member.toCreateMap())
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.WRITE, "members")
                }
            }
        }
        return result
    }

    // Reset password using registered phone number or ID number
    fun resetPassword(identifierPhoneOrId: String, newPassword: String): Result<Unit> {
        val res = store.resetPassword(identifierPhoneOrId, newPassword)
        if (res.isSuccess) {
            _membersFlow.value = store.getAllMembers()
        }
        return res
    }

    fun recordMonthlyContribution(memberId: String, month: Int): Result<Member> {
        val result = store.recordMonthlyContribution(memberId, month)
        if (result.isSuccess) {
            val updated = result.getOrThrow()
            if (_activeMemberFlow.value?.id == memberId) {
                _activeMemberFlow.value = updated
            }
            _membersFlow.value = store.getAllMembers()
            if (auth.currentUser != null) {
                try {
                    db.collection("members").document(updated.id).set(updated.toUpdateMap())
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.WRITE, "members")
                }
            }
        }
        return result
    }

    fun toggleMonthlyContribution(memberId: String, month: Int): Result<Member> {
        val result = store.toggleMonthlyContribution(memberId, month)
        if (result.isSuccess) {
            val updated = result.getOrThrow()
            if (_activeMemberFlow.value?.id == memberId) {
                _activeMemberFlow.value = updated
            }
            _membersFlow.value = store.getAllMembers()
            if (auth.currentUser != null) {
                try {
                    db.collection("members").document(updated.id).set(updated.toUpdateMap())
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.WRITE, "members")
                }
            }
        }
        return result
    }

    fun observeMember(userId: String): Flow<Member?> = activeMemberFlow

    fun observeAllMembers(): Flow<List<Member>> = _membersFlow.asStateFlow()

    fun observeUserCases(userId: String): Flow<List<WelfareCase>> {
        val memberId = _activeMemberFlow.value?.id ?: userId
        val userList = _casesFlow.value.filter { it.memberId == memberId || it.userId == memberId }
        val flow = MutableStateFlow(userList)
        return flow.asStateFlow()
    }

    fun observeAllCases(): Flow<List<WelfareCase>> = _casesFlow.asStateFlow()

    suspend fun saveMember(member: Member): Result<Unit> {
        store.updateMember(member)
        if (_activeMemberFlow.value?.id == member.id) {
            _activeMemberFlow.value = member
        }
        _membersFlow.value = store.getAllMembers()

        if (auth.currentUser != null) {
            try {
                db.collection("members").document(member.id).set(member.toUpdateMap()).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "members/${member.id}")
            }
        }
        return Result.success(Unit)
    }

    suspend fun submitCase(welfareCase: WelfareCase): Result<String> {
        val activeMember = _activeMemberFlow.value
        val memberId = activeMember?.id ?: welfareCase.memberId
        val finalCase = welfareCase.copy(
            id = "CASE-${(1000..9999).random()}",
            memberId = memberId,
            userId = memberId
        )
        store.addCase(finalCase)
        _casesFlow.value = store.getAllCases()

        if (auth.currentUser != null) {
            try {
                db.collection("cases").document(finalCase.id).set(finalCase.toCreateMap()).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "cases")
            }
        }
        return Result.success(finalCase.id)
    }

    suspend fun updateCase(
        caseId: String,
        updatedStatus: String,
        approvedAmount: Double,
        officerNotes: String,
        disbursementDate: String
    ): Result<Unit> {
        store.updateCase(caseId, updatedStatus, approvedAmount, officerNotes, disbursementDate)
        _casesFlow.value = store.getAllCases()

        if (auth.currentUser != null) {
            try {
                db.collection("cases").document(caseId).update(
                    mapOf(
                        "status" to updatedStatus,
                        "approvedAmount" to approvedAmount,
                        "officerNotes" to officerNotes,
                        "disbursementDate" to disbursementDate
                    )
                ).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "cases/$caseId")
            }
        }
        return Result.success(Unit)
    }

    fun logout() {
        store.setActiveMember(null)
        _activeMemberFlow.value = null
        adminSessionActive = false
        auth.signOut()
    }
}
