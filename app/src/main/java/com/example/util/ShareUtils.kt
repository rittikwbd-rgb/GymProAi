package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DietPlan
import com.example.data.model.Member
import com.example.data.model.WorkoutPlan
import java.io.File

object ShareUtils {

    fun shareInvoiceOnWhatsApp(context: Context, member: Member, gymName: String = "Metro Fitness Club") {
        try {
            val pdfFile = PdfUtils.generateInvoicePdfFile(context, member, gymName)
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val invoiceMsg = """
                🧾 *OFFICIAL MEMBERSHIP INVOICE & RECEIPT*
                🏢 *$gymName*
                
                👤 *Member:* ${member.name}
                📱 *Phone:* ${member.phone}
                📋 *Package:* ${member.membershipPlan}
                💳 *Status:* ${member.paymentStatus}
                📅 *Joined:* ${member.joiningDate}
                ⌛ *Expires:* ${member.expiryDate}
                
                Attached is your official graphical PDF receipt & tax invoice. Thank you for training with us!
            """.trimIndent()

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, invoiceMsg)
                `package` = "com.whatsapp"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(whatsappIntent)
            Toast.makeText(context, "Opening WhatsApp to send invoice to ${member.name}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val pdfFile = PdfUtils.generateInvoicePdfFile(context, member, gymName)
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Official Membership Invoice for ${member.name} - $gymName")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Invoice via"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Invoice sharing error: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun shareInvoiceOnWhatsAppForInvoice(context: Context, invoice: com.example.data.model.GymInvoice, gymName: String = "Metro Fitness Club") {
        try {
            val pdfFile = PdfUtils.generateInvoicePdfFileForInvoice(context, invoice, gymName)
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val invoiceMsg = """
                🧾 *OFFICIAL MEMBERSHIP INVOICE & RECEIPT*
                🏢 *$gymName*
                
                📄 *Invoice #:* ${invoice.id}
                👤 *Member:* ${invoice.memberName}
                📱 *Phone:* ${invoice.memberPhone}
                📋 *Package:* ${invoice.packageName}
                💰 *Amount:* ₹${String.format("%.2f", invoice.totalAmount)}
                💳 *Status:* ${invoice.paymentStatus} (${invoice.paymentMode})
                📅 *Date:* ${invoice.date}
                
                Attached is your official graphical PDF receipt & tax invoice. Thank you for training with us!
            """.trimIndent()

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, invoiceMsg)
                `package` = "com.whatsapp"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(whatsappIntent)
            Toast.makeText(context, "Opening WhatsApp to send invoice ${invoice.id}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val pdfFile = PdfUtils.generateInvoicePdfFileForInvoice(context, invoice, gymName)
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Official Membership Invoice ${invoice.id} for ${invoice.memberName} - $gymName")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Invoice via"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Invoice sharing error: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun shareInvoicePdfForInvoice(context: Context, invoice: com.example.data.model.GymInvoice, gymName: String = "Metro Fitness Club") {
        try {
            val pdfFile = PdfUtils.generateInvoicePdfFileForInvoice(context, invoice, gymName)
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Official Membership Invoice ${invoice.id} for ${invoice.memberName} - $gymName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(chooserIntent, "View / Share Invoice PDF"))
        } catch (ex: Exception) {
            Toast.makeText(context, "Error opening PDF: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareWorkoutOnWhatsApp(context: Context, plan: WorkoutPlan, gymName: String = "Metro Fitness Club") {
        try {
            val pdfFile = PdfUtils.generatePdfReportFile(
                context = context,
                dietPlan = null,
                workoutPlan = plan,
                gymName = gymName
            )
            sharePdfFileToWhatsApp(context, pdfFile, "GymAI Pro Workout Plan - ${plan.memberName}")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDietOnWhatsApp(context: Context, plan: DietPlan, gymName: String = "Metro Fitness Club") {
        try {
            val pdfFile = PdfUtils.generatePdfReportFile(
                context = context,
                dietPlan = plan,
                workoutPlan = null,
                gymName = gymName
            )
            sharePdfFileToWhatsApp(context, pdfFile, "GymAI Pro Diet Plan - ${plan.memberName}")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareReportOnWhatsApp(
        context: Context,
        dietPlan: DietPlan?,
        workoutPlan: WorkoutPlan?,
        gymName: String = "Metro Fitness Club"
    ) {
        try {
            val pdfFile = PdfUtils.generatePdfReportFile(
                context = context,
                dietPlan = dietPlan,
                workoutPlan = workoutPlan,
                gymName = gymName
            )
            val memberName = dietPlan?.memberName ?: workoutPlan?.memberName ?: "Client"
            sharePdfFileToWhatsApp(context, pdfFile, "GymAI Pro Report - $memberName")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdfFileToWhatsApp(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Attached is the official graphical PDF report from GymAI Pro.")
                `package` = "com.whatsapp"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(whatsappIntent)
            Toast.makeText(context, "Sending Graphical PDF via WhatsApp...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback to general intent chooser if WhatsApp isn't installed
            try {
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, "Attached is the official graphical PDF report from GymAI Pro.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Graphical PDF Report via"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to share PDF: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
