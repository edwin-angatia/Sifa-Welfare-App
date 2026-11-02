package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Member
import com.example.data.model.WelfareCase
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun escapeCsv(value: Any?): String {
        if (value == null) return "\"\""
        val str = value.toString().replace("\"", "\"\"")
        return "\"$str\""
    }

    // Export members record into Excel-compatible CSV with UTF-8 BOM
    fun exportMembersToCsv(context: Context, members: List<Member>): Uri {
        val fileName = "welfare_members_${fileTimestamp.format(Date())}.csv"
        val file = File(getReportsDir(context), fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Write UTF-8 BOM for Microsoft Excel auto-encoding detection
                writer.write("\uFEFF")
                writer.write(
                    listOf(
                        "Member Number",
                        "National ID Number",
                        "Full Legal Name",
                        "Phone Number",
                        "Monthly Contribution (Ksh)",
                        "Paid Months (Jan-Dec)",
                        "Total Paid (Ksh)",
                        "Active Status",
                        "Category",
                        "Status",
                        "Household Size",
                        "Dependants Details",
                        "Next of Kin Name",
                        "Next of Kin Phone",
                        "Next of Kin Relationship",
                        "Next of Kin Address",
                        "Physical Address",
                        "Notes"
                    ).joinToString(",") + "\n"
                )

                for (m in members) {
                    val dependantsStr = m.dependants.joinToString("; ") {
                        "${it.name} (${it.relationship}, age ${it.age})"
                    }
                    val paidMonthsStr = if (m.paidMonths.isEmpty()) "None" else m.paidMonths.sorted().joinToString(", ") { monthNum ->
                        com.example.data.model.ContributionHelper.getMonthName(monthNum)
                    }
                    val totalPaidKsh = m.paidMonths.size * m.contributionRateKsh
                    val line = listOf(
                        escapeCsv(m.memberNumber),
                        escapeCsv(m.idNumber),
                        escapeCsv(m.fullName),
                        escapeCsv(m.phone),
                        escapeCsv(m.contributionRateKsh),
                        escapeCsv(paidMonthsStr),
                        escapeCsv(totalPaidKsh),
                        escapeCsv(m.getActiveUntilText()),
                        escapeCsv(m.category),
                        escapeCsv(m.status),
                        escapeCsv(m.householdSize),
                        escapeCsv(dependantsStr),
                        escapeCsv(m.nextOfKin?.name ?: ""),
                        escapeCsv(m.nextOfKin?.phone ?: ""),
                        escapeCsv(m.nextOfKin?.relationship ?: ""),
                        escapeCsv(m.nextOfKin?.address ?: ""),
                        escapeCsv(m.address),
                        escapeCsv(m.notes)
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Export case history into Excel-compatible CSV
    fun exportCasesToCsv(
        context: Context,
        cases: List<WelfareCase>,
        membersMap: Map<String, Member> = emptyMap()
    ): Uri {
        val fileName = "welfare_cases_${fileTimestamp.format(Date())}.csv"
        val file = File(getReportsDir(context), fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(
                    listOf(
                        "Case Number",
                        "Member Name",
                        "Member Number",
                        "Title",
                        "Category",
                        "Status",
                        "Priority",
                        "Requested Amount (Ksh)",
                        "Approved Amount (Ksh)",
                        "Disbursement Date",
                        "Hardship Description",
                        "Officer Notes"
                    ).joinToString(",") + "\n"
                )

                for (c in cases) {
                    val member = membersMap[c.memberId]
                    val line = listOf(
                        escapeCsv(c.caseNumber),
                        escapeCsv(member?.fullName ?: "Member"),
                        escapeCsv(member?.memberNumber ?: c.memberId),
                        escapeCsv(c.title),
                        escapeCsv(c.category),
                        escapeCsv(c.status),
                        escapeCsv(c.priority),
                        escapeCsv(String.format(Locale.US, "%.2f", c.requestedAmount)),
                        escapeCsv(String.format(Locale.US, "%.2f", c.approvedAmount)),
                        escapeCsv(c.disbursementDate),
                        escapeCsv(c.description),
                        escapeCsv(c.officerNotes)
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Export official PDF report using standard Android PdfDocument
    fun exportOfficialReportToPdf(
        context: Context,
        members: List<Member>,
        cases: List<WelfareCase>,
        reportTitle: String = "Welfare Department Official Case & Member Report"
    ): Uri {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var yPos = 40f

        // Header Background Banner
        paint.color = Color.parseColor("#0091EA")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Title text
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SIFA WELFARE - MEMBER & CASE REPORT", 30f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Star Initiative for Social and Economic Assistance", 30f, 58f, paint)
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 380f, 58f, paint)

        yPos = 115f

        // Summary Metric Cards
        paint.color = Color.parseColor("#E1F5FE")
        canvas.drawRoundRect(30f, yPos, 195f, yPos + 60f, 8f, 8f, paint)
        canvas.drawRoundRect(210f, yPos, 385f, yPos + 60f, 8f, 8f, paint)
        canvas.drawRoundRect(400f, yPos, 565f, yPos + 60f, 8f, 8f, paint)

        paint.color = Color.parseColor("#0077C2")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${members.size}", 45f, yPos + 32f, paint)
        canvas.drawText("${cases.size}", 225f, yPos + 32f, paint)

        val totalContributionsKsh = members.sumOf { it.paidMonths.size * it.contributionRateKsh }
        canvas.drawText("Ksh $totalContributionsKsh", 415f, yPos + 32f, paint)

        paint.color = Color.parseColor("#546E7A")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Members", 45f, yPos + 48f, paint)
        canvas.drawText("Active Cases", 225f, yPos + 48f, paint)
        canvas.drawText("Contributions Pool", 415f, yPos + 48f, paint)

        yPos += 85f

        // Section: Case History
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RECENT WELFARE CASE HISTORIES", 30f, yPos, paint)

        yPos += 15f
        // Table Header
        paint.color = Color.parseColor("#00897B")
        canvas.drawRect(30f, yPos, 565f, yPos + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CASE #", 35f, yPos + 15f, paint)
        canvas.drawText("TITLE & HARDSHIP", 100f, yPos + 15f, paint)
        canvas.drawText("CATEGORY", 280f, yPos + 15f, paint)
        canvas.drawText("STATUS", 400f, yPos + 15f, paint)
        canvas.drawText("AMOUNT", 490f, yPos + 15f, paint)

        yPos += 24f

        val displayCases = cases.take(12)
        var isAlt = false
        paint.textSize = 9f

        for (c in displayCases) {
            paint.color = if (isAlt) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(30f, yPos, 565f, yPos + 20f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(c.caseNumber.take(10), 35f, yPos + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val titleTrimmed = if (c.title.length > 28) c.title.take(26) + "..." else c.title
            canvas.drawText(titleTrimmed, 100f, yPos + 14f, paint)
            canvas.drawText(c.category.take(18), 280f, yPos + 14f, paint)

            paint.color = when (c.status) {
                "Approved" -> Color.parseColor("#10B981")
                "Disbursed" -> Color.parseColor("#6366F1")
                "Under Review" -> Color.parseColor("#F59E0B")
                "Rejected" -> Color.parseColor("#EF4444")
                else -> Color.parseColor("#0284C7")
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(c.status, 400f, yPos + 14f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val amtStr = "Ksh ${String.format(Locale.US, "%,.0f", if (c.approvedAmount > 0) c.approvedAmount else c.requestedAmount)}"
            canvas.drawText(amtStr, 490f, yPos + 14f, paint)

            yPos += 20f
            isAlt = !isAlt
        }

        // Footer / Signature Section
        yPos = 720f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(30f, yPos, 565f, yPos, paint)

        yPos += 25f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Prepared by Sifa Welfare Administration (Star Initiative)", 30f, yPos, paint)
        canvas.drawText("Authorized Welfare Officer Signature: _______________________", 250f, yPos, paint)

        yPos += 18f
        canvas.drawText("Confidential certified document for welfare audits, committee records, and member tracking.", 30f, yPos, paint)

        pdfDocument.finishPage(page)

        val fileName = "sifa_welfare_report_${fileTimestamp.format(Date())}.pdf"
        val file = File(getReportsDir(context), fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Generate Official Sifa Welfare Constitution & Bylaws PDF Document
    fun generateConstitutionRulesPdf(context: Context): Uri {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Banner Header
        paint.color = Color.parseColor("#00838F")
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SIFA WELFARE - CONSTITUTION & OPERATIONAL RULES", 30f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Star Initiative for Social and Economic Assistance • Official Registered Bylaws 2026", 30f, 60f, paint)
        canvas.drawText("Effective Date: November 2026 • Edition 3.1", 30f, 75f, paint)

        var y = 120f
        fun drawSection(heading: String, bodyLines: List<String>) {
            paint.color = Color.parseColor("#006064")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(heading, 30f, y, paint)
            y += 16f

            paint.color = Color.parseColor("#334155")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (line in bodyLines) {
                canvas.drawText(line, 35f, y, paint)
                y += 14f
            }
            y += 10f
        }

        drawSection(
            "ARTICLE I: NAME, VISION AND MOTTO",
            listOf(
                "1.1 The association is designated 'Sifa Welfare' (Star Initiative for Social & Economic Assistance).",
                "1.2 Motto: 'United in Uplifting Every Member' - grounded in dignity, empathy, and collective strength.",
                "1.3 Official Operations Center: Registered community secretariat in Nairobi with regional chapter branches."
            )
        )

        drawSection(
            "ARTICLE II: MONTHLY CONTRIBUTIONS & FINANCIAL DUES",
            listOf(
                "2.1 Dues Rate: Every registered member contributes a mandatory statutory sum of Ksh 100 per calendar month.",
                "2.2 Contribution Schedule: Payable from January through December each calendar year (Total Ksh 1,200 per year).",
                "2.3 Payment Method: Direct mobile payment via official M-Pesa Paybill (400200) / SIM Toolkit with auto-reconciliation.",
                "2.4 Solvency Guarantee: 100% of collected monthly dues are ring-fenced for member assistance and welfare reserves."
            )
        )

        drawSection(
            "ARTICLE III: WELFARE ASSISTANCE CATEGORIES & CASE APPROVALS",
            listOf(
                "3.1 Emergency Medical & Hospital Aid: Assistance for verified in-patient care, surgery, and critical medications.",
                "3.2 Bereavement & Final Respects: Dignified grant for passing of principal member, registered spouse, child or parent.",
                "3.3 Children's Education Grants: Annual term subsidies for primary and secondary schooling of registered dependants.",
                "3.4 Application Process: Cases submitted securely through the Sifa Welfare application; audited and signed by the Admin."
            )
        )

        drawSection(
            "ARTICLE IV: RIGHTS OF REGISTERED MEMBERS & NEXT OF KIN",
            listOf(
                "4.1 Full demographic security: National ID and phone numbers are encrypted with salted hash protocols.",
                "4.2 Dependants registry: Up to 6 dependants and primary Next-of-Kin enjoy direct standing in welfare claims.",
                "4.3 Transparent audit: Members hold the right to review published welfare statements and official PDF reports."
            )
        )

        // Seal & Signatures
        y = 730f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(30f, y, 565f, y, paint)

        y += 20f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SIFA WELFARE EXECUTIVE COMMITTEE", 30f, y, paint)
        canvas.drawText("OFFICIAL SEAL & STAMP", 400f, y, paint)

        y += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Certified true copy of the welfare society constitution.", 30f, y, paint)
        canvas.drawText("Admin PIN Authentication: VERIFIED", 400f, y, paint)

        pdfDocument.finishPage(page)

        val fileName = "sifa_welfare_constitution_bylaws.pdf"
        val file = File(getReportsDir(context), fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareReport(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
