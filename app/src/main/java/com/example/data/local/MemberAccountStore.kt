package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Dependant
import com.example.data.model.Member
import com.example.data.model.NextOfKin
import com.example.data.model.WelfareCase
import com.example.util.SecurityUtils
import org.json.JSONArray
import org.json.JSONObject

class MemberAccountStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sifa_welfare_accounts", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MEMBERS_JSON = "members_json"
        private const val KEY_ACTIVE_MEMBER_ID = "active_member_id"
        private const val KEY_CASES_JSON = "cases_json"
    }

    init {
        if (!prefs.contains(KEY_MEMBERS_JSON)) {
            seedInitialData()
        }
    }

    private fun seedInitialData() {
        val salt = SecurityUtils.generateSalt()
        val defaultHash = SecurityUtils.hashPassword("123456", salt)

        val demoMember = Member(
            id = "MEM-001",
            userId = "MEM-001",
            idNumber = "28475931",
            fullName = "Morio Otieno",
            memberNumber = "SIFA-2026-001",
            phone = "0712345678",
            passwordHash = defaultHash,
            passwordSalt = salt,
            category = "Low Income Family",
            householdSize = 4,
            monthlyIncome = 1500.0,
            address = "Kibera Sector 4, Nairobi",
            paidMonths = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), // Active till end of Nov
            dependants = listOf(
                Dependant(name = "Brian Otieno", relationship = "Son", age = 8, specialNeeds = "None"),
                Dependant(name = "Faith Otieno", relationship = "Daughter", age = 12, specialNeeds = "School Aid")
            ),
            nextOfKin = NextOfKin(
                name = "David Otieno",
                relationship = "Spouse",
                phone = "0722334455",
                address = "Kibera Sector 4"
            )
        )

        val demoCase = WelfareCase(
            id = "CASE-101",
            memberId = "MEM-001",
            userId = "MEM-001",
            caseNumber = "CASE-101",
            title = "Primary School Education Grant",
            category = "Education Aid",
            status = "Approved",
            priority = "Normal",
            requestedAmount = 300.0,
            approvedAmount = 300.0,
            description = "Tuition support and textbooks for Grade 6 entrance.",
            officerNotes = "Approved under Sifa Welfare Community Fund Q4",
            disbursementDate = "2026-10-15"
        )

        saveMemberList(listOf(demoMember))
        saveCaseList(listOf(demoCase))
    }

    fun getAllMembers(): List<Member> {
        val jsonStr = prefs.getString(KEY_MEMBERS_JSON, "[]") ?: "[]"
        val list = mutableListOf<Member>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(parseMember(obj))
        }
        return list
    }

    fun getActiveMember(): Member? {
        val activeId = prefs.getString(KEY_ACTIVE_MEMBER_ID, null) ?: return null
        return getAllMembers().find { it.id == activeId || it.idNumber == activeId }
    }

    fun setActiveMember(member: Member?) {
        prefs.edit().putString(KEY_ACTIVE_MEMBER_ID, member?.id).apply()
    }

    fun authenticateMember(identifier: String, password: String): Member? {
        val cleanIdentifier = identifier.trim()
        val members = getAllMembers()
        val member = members.find {
            it.idNumber.equals(cleanIdentifier, ignoreCase = true) ||
            it.phone.equals(cleanIdentifier, ignoreCase = true) ||
            it.memberNumber.equals(cleanIdentifier, ignoreCase = true)
        } ?: return null

        val isValid = SecurityUtils.verifyPassword(password, member.passwordSalt, member.passwordHash)
        return if (isValid) member else null
    }

    fun registerMember(
        idNumber: String,
        phone: String,
        fullName: String,
        password: String,
        dependants: List<Dependant>,
        nextOfKin: NextOfKin?,
        category: String = "General Member",
        address: String = ""
    ): Result<Member> {
        val existing = getAllMembers().find {
            it.idNumber.equals(idNumber.trim(), ignoreCase = true) ||
            it.phone.equals(phone.trim(), ignoreCase = true)
        }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("A member with this ID number or Phone number is already registered."))
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val memberId = "MEM-${System.currentTimeMillis() % 1000000}"
        val memberNum = "SIFA-2026-${(1000..9999).random()}"

        val newMember = Member(
            id = memberId,
            userId = memberId,
            idNumber = idNumber.trim(),
            fullName = fullName.trim(),
            memberNumber = memberNum,
            phone = phone.trim(),
            passwordHash = hash,
            passwordSalt = salt,
            category = category,
            householdSize = (dependants.size + 1).toLong(),
            address = address.trim(),
            dependants = dependants,
            nextOfKin = nextOfKin,
            paidMonths = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), // Active till end of Nov
            contributionRateKsh = 100,
            status = "Active"
        )

        val updatedList = getAllMembers().toMutableList().apply { add(newMember) }
        saveMemberList(updatedList)
        setActiveMember(newMember)
        return Result.success(newMember)
    }

    fun recordMonthlyContribution(memberId: String, month: Int): Result<Member> {
        val members = getAllMembers().toMutableList()
        val index = members.indexOfFirst { it.id == memberId }
        if (index == -1) return Result.failure(IllegalArgumentException("Member not found"))

        val member = members[index]
        val currentPaid = member.paidMonths.toMutableSet()
        currentPaid.add(month)
        val updated = member.copy(paidMonths = currentPaid.sorted())
        members[index] = updated
        saveMemberList(members)
        if (getActiveMember()?.id == memberId) {
            setActiveMember(updated)
        }
        return Result.success(updated)
    }

    fun toggleMonthlyContribution(memberId: String, month: Int): Result<Member> {
        val members = getAllMembers().toMutableList()
        val index = members.indexOfFirst { it.id == memberId }
        if (index == -1) return Result.failure(IllegalArgumentException("Member not found"))

        val member = members[index]
        val currentPaid = member.paidMonths.toMutableSet()
        if (currentPaid.contains(month)) {
            currentPaid.remove(month)
        } else {
            currentPaid.add(month)
        }
        val updated = member.copy(paidMonths = currentPaid.sorted())
        members[index] = updated
        saveMemberList(members)
        if (getActiveMember()?.id == memberId) {
            setActiveMember(updated)
        }
        return Result.success(updated)
    }

    fun resetPassword(identifierPhoneOrId: String, newPassword: String): Result<Unit> {
        val clean = identifierPhoneOrId.trim()
        val members = getAllMembers().toMutableList()
        val index = members.indexOfFirst {
            it.phone.equals(clean, ignoreCase = true) || it.idNumber.equals(clean, ignoreCase = true)
        }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("No registered member found with this ID or Phone number."))
        }

        val member = members[index]
        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        val updatedMember = member.copy(
            passwordHash = newHash,
            passwordSalt = newSalt
        )
        members[index] = updatedMember
        saveMemberList(members)
        return Result.success(Unit)
    }

    fun updateMember(member: Member) {
        val members = getAllMembers().toMutableList()
        val index = members.indexOfFirst { it.id == member.id }
        if (index != -1) {
            members[index] = member
            saveMemberList(members)
        }
    }

    fun getAllCases(): List<WelfareCase> {
        val jsonStr = prefs.getString(KEY_CASES_JSON, "[]") ?: "[]"
        val list = mutableListOf<WelfareCase>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(parseCase(obj))
        }
        return list
    }

    fun addCase(welfareCase: WelfareCase) {
        val cases = getAllCases().toMutableList()
        cases.add(0, welfareCase)
        saveCaseList(cases)
    }

    fun updateCase(caseId: String, status: String, approvedAmount: Double, notes: String, disbursementDate: String) {
        val cases = getAllCases().toMutableList()
        val index = cases.indexOfFirst { it.id == caseId }
        if (index != -1) {
            val updated = cases[index].copy(
                status = status,
                approvedAmount = approvedAmount,
                officerNotes = notes,
                disbursementDate = disbursementDate
            )
            cases[index] = updated
            saveCaseList(cases)
        }
    }

    private fun saveMemberList(list: List<Member>) {
        val array = JSONArray()
        for (m in list) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("userId", m.userId)
                put("idNumber", m.idNumber)
                put("fullName", m.fullName)
                put("memberNumber", m.memberNumber)
                put("phone", m.phone)
                put("email", m.email)
                put("passwordHash", m.passwordHash)
                put("passwordSalt", m.passwordSalt)
                put("category", m.category)
                put("status", m.status)
                put("householdSize", m.householdSize)
                put("monthlyIncome", m.monthlyIncome)
                put("address", m.address)
                put("emergencyContact", m.emergencyContact)
                put("notes", m.notes)
                put("contributionRateKsh", m.contributionRateKsh)

                val monthsArr = JSONArray()
                m.paidMonths.forEach { monthsArr.put(it) }
                put("paidMonths", monthsArr)

                val depsArr = JSONArray()
                for (d in m.dependants) {
                    depsArr.put(JSONObject().apply {
                        put("name", d.name)
                        put("relationship", d.relationship)
                        put("age", d.age)
                        put("specialNeeds", d.specialNeeds)
                    })
                }
                put("dependants", depsArr)

                if (m.nextOfKin != null) {
                    put("nextOfKin", JSONObject().apply {
                        put("name", m.nextOfKin.name)
                        put("relationship", m.nextOfKin.relationship)
                        put("phone", m.nextOfKin.phone)
                        put("address", m.nextOfKin.address)
                    })
                }
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MEMBERS_JSON, array.toString()).apply()
    }

    private fun saveCaseList(list: List<WelfareCase>) {
        val array = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("memberId", c.memberId)
                put("userId", c.userId)
                put("caseNumber", c.caseNumber)
                put("title", c.title)
                put("category", c.category)
                put("status", c.status)
                put("priority", c.priority)
                put("requestedAmount", c.requestedAmount)
                put("approvedAmount", c.approvedAmount)
                put("description", c.description)
                put("officerNotes", c.officerNotes)
                put("disbursementDate", c.disbursementDate)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CASES_JSON, array.toString()).apply()
    }

    private fun parseMember(obj: JSONObject): Member {
        val depsList = mutableListOf<Dependant>()
        if (obj.has("dependants")) {
            val depsArr = obj.getJSONArray("dependants")
            for (j in 0 until depsArr.length()) {
                val dObj = depsArr.getJSONObject(j)
                depsList.add(
                    Dependant(
                        name = dObj.optString("name"),
                        relationship = dObj.optString("relationship", "Child"),
                        age = dObj.optInt("age", 0),
                        specialNeeds = dObj.optString("specialNeeds")
                    )
                )
            }
        }

        var nextOfKin: NextOfKin? = null
        if (obj.has("nextOfKin") && !obj.isNull("nextOfKin")) {
            val kObj = obj.getJSONObject("nextOfKin")
            nextOfKin = NextOfKin(
                name = kObj.optString("name"),
                relationship = kObj.optString("relationship", "Spouse"),
                phone = kObj.optString("phone"),
                address = kObj.optString("address")
            )
        }

        val paidMonthsList = mutableListOf<Int>()
        if (obj.has("paidMonths")) {
            val mArr = obj.getJSONArray("paidMonths")
            for (j in 0 until mArr.length()) {
                paidMonthsList.add(mArr.getInt(j))
            }
        } else {
            paidMonthsList.addAll(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
        }

        return Member(
            id = obj.optString("id"),
            userId = obj.optString("userId"),
            idNumber = obj.optString("idNumber"),
            fullName = obj.optString("fullName"),
            memberNumber = obj.optString("memberNumber"),
            phone = obj.optString("phone"),
            email = obj.optString("email"),
            passwordHash = obj.optString("passwordHash"),
            passwordSalt = obj.optString("passwordSalt"),
            category = obj.optString("category", "General Member"),
            status = obj.optString("status", "Active"),
            householdSize = obj.optLong("householdSize", 1L),
            monthlyIncome = obj.optDouble("monthlyIncome", 0.0),
            address = obj.optString("address"),
            emergencyContact = obj.optString("emergencyContact"),
            notes = obj.optString("notes"),
            paidMonths = paidMonthsList,
            contributionRateKsh = obj.optInt("contributionRateKsh", 100),
            dependants = depsList,
            nextOfKin = nextOfKin
        )
    }

    private fun parseCase(obj: JSONObject): WelfareCase {
        return WelfareCase(
            id = obj.optString("id"),
            memberId = obj.optString("memberId"),
            userId = obj.optString("userId"),
            caseNumber = obj.optString("caseNumber"),
            title = obj.optString("title"),
            category = obj.optString("category", "Financial Aid"),
            status = obj.optString("status", "Submitted"),
            priority = obj.optString("priority", "Normal"),
            requestedAmount = obj.optDouble("requestedAmount", 0.0),
            approvedAmount = obj.optDouble("approvedAmount", 0.0),
            description = obj.optString("description"),
            officerNotes = obj.optString("officerNotes"),
            disbursementDate = obj.optString("disbursementDate")
        )
    }

    // Admin PIN Management
    fun getAdminPin(): String {
        return prefs.getString("admin_secret_pin", "9922") ?: "9922"
    }

    fun setAdminPin(newPin: String): Boolean {
        if (newPin.trim().length < 4) return false
        prefs.edit().putString("admin_secret_pin", newPin.trim()).apply()
        return true
    }

    fun verifyAdminPin(enteredPin: String): Boolean {
        val clean = enteredPin.trim()
        val currentPin = getAdminPin().trim()
        return clean.isNotBlank() && clean == currentPin
    }

    // PDF Constitution and Bylaws Document Metadata
    fun getConstitutionDocTitle(): String {
        return prefs.getString("constitution_pdf_title", "Sifa Welfare Constitution & Bylaws (2026 Edition)") ?: "Sifa Welfare Constitution & Bylaws (2026 Edition)"
    }

    fun getConstitutionDocFileName(): String {
        return prefs.getString("constitution_pdf_filename", "sifa_welfare_rules_bylaws.pdf") ?: "sifa_welfare_rules_bylaws.pdf"
    }

    fun getConstitutionDocDate(): String {
        return prefs.getString("constitution_pdf_date", "Nov 2026") ?: "Nov 2026"
    }

    fun getConstitutionDocUri(): String? {
        return prefs.getString("constitution_pdf_uri", null)
    }

    fun saveConstitutionPdfInfo(title: String, filename: String, date: String, uriString: String?) {
        prefs.edit()
            .putString("constitution_pdf_title", title)
            .putString("constitution_pdf_filename", filename)
            .putString("constitution_pdf_date", date)
            .putString("constitution_pdf_uri", uriString)
            .apply()
    }
}
