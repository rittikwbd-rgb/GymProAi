package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AnalyticsSummary
import com.example.data.model.DietPlan
import com.example.data.model.Member
import com.example.data.model.WorkoutPlan
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    fun generatePdfReportFile(
        context: Context,
        dietPlan: DietPlan?,
        workoutPlan: WorkoutPlan?,
        analytics: AnalyticsSummary? = null,
        aiInsight: String? = null,
        gymName: String = "Metro Fitness Club"
    ): File {
        val pdfDocument = PdfDocument()
        val memberName = dietPlan?.memberName ?: workoutPlan?.memberName ?: "Sarah Jenkins"
        val paint = Paint().apply { isAntiAlias = true }

        var totalPages = 1
        if (dietPlan != null && workoutPlan != null) {
            totalPages = 2
        }

        // PAGE 1: DIET PLAN / GENERAL DOSSIER / ANALYTICS
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        drawPageHeader(canvas1, paint, gymName, "PAGE 1 OF $totalPages")

        var y = 80f

        if (dietPlan != null || workoutPlan != null) {
            // Client Recomposition Dossier
            paint.color = Color.parseColor("#F4F6F8")
            canvas1.drawRoundRect(RectF(36f, y, 559f, y + 54f), 8f, 8f, paint)

            // Accent Left Bar
            paint.color = Color.parseColor("#0D5C3A")
            canvas1.drawRoundRect(RectF(36f, y, 42f, y + 54f), 4f, 4f, paint)

            paint.color = Color.parseColor("#0D5C3A")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText("CLIENT BODY RECOMPOSITION & NUTRITION DOSSIER", 52f, y + 20f, paint)

            paint.color = Color.BLACK
            paint.textSize = 9.5f
            paint.typeface = Typeface.DEFAULT
            val goalStr = dietPlan?.goal ?: workoutPlan?.goal ?: "Fat Loss & Muscle Recomposition"
            canvas1.drawText("Member: $memberName   |   Goal: $goalStr", 52f, y + 36f, paint)

            val statsLine = if (dietPlan != null) {
                "Age: ${dietPlan.age} yrs   Weight: ${dietPlan.weightKg} kg   Height: ${dietPlan.heightCm} cm   BMI: ${dietPlan.bmi}   BMR: ${dietPlan.bmr} kcal"
            } else {
                "Days/Week: ${workoutPlan?.daysPerWeek ?: 4}   Split: Upper / Lower / Full Body"
            }
            paint.color = Color.DKGRAY
            paint.textSize = 8.5f
            canvas1.drawText(statsLine, 52f, y + 48f, paint)

            y += 66f

            // GRAPHICAL KPI METRICS CARDS
            val cals = dietPlan?.targetCalories ?: 1850
            val pGrams = dietPlan?.proteinGrams ?: 145
            val cGrams = dietPlan?.carbsGrams ?: 180
            val fGrams = dietPlan?.fatGrams ?: 55

            val cardWidth = 122f
            val cardGap = 12f

            drawMetricCard(canvas1, paint, 36f, y, cardWidth, 52f, "TARGET CALORIES", "$cals kcal", "#0D5C3A")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap), y, cardWidth, 52f, "PROTEIN TARGET", "${pGrams}g", "#1E88E5")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap) * 2, y, cardWidth, 52f, "CARBS TARGET", "${cGrams}g", "#FB8C00")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap) * 3, y, cardWidth, 52f, "FATS TARGET", "${fGrams}g", "#E53935")

            y += 64f

            // MACRONUTRIENT RATIO GRAPHICAL BAR CHART
            paint.color = Color.BLACK
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText("1. MACRONUTRIENT RATIO BREAKDOWN & HYDRATION", 36f, y, paint)

            y += 12f

            val totalMacros = (pGrams + cGrams + fGrams).coerceAtLeast(1)
            val pPct = (pGrams.toFloat() / totalMacros) * 100f
            val cPct = (cGrams.toFloat() / totalMacros) * 100f
            val fPct = (fGrams.toFloat() / totalMacros) * 100f

            val barWidth = 523f
            val pWidth = barWidth * (pPct / 100f)
            val cWidth = barWidth * (cPct / 100f)

            // Bar background rounded container
            paint.color = Color.parseColor("#E0E0E0")
            canvas1.drawRoundRect(RectF(36f, y, 559f, y + 16f), 4f, 4f, paint)

            // Protein Bar
            paint.color = Color.parseColor("#1E88E5")
            canvas1.drawRect(RectF(36f, y, 36f + pWidth, y + 16f), paint)

            // Carbs Bar
            paint.color = Color.parseColor("#FB8C00")
            canvas1.drawRect(RectF(36f + pWidth, y, 36f + pWidth + cWidth, y + 16f), paint)

            // Fats Bar
            paint.color = Color.parseColor("#E53935")
            canvas1.drawRect(RectF(36f + pWidth + cWidth, y, 559f, y + 16f), paint)

            y += 26f
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.DKGRAY
            canvas1.drawText("■ Protein: ${pPct.toInt()}% (${pGrams}g)    ■ Carbs: ${cPct.toInt()}% (${cGrams}g)    ■ Fats: ${fPct.toInt()}% (${fGrams}g)    ■ Hydration Goal: ${dietPlan?.waterLiters ?: 3.5}L/day", 36f, y, paint)

            // MEALS GRAPHICAL TABLE
            if (dietPlan != null && dietPlan.meals.isNotEmpty()) {
                y += 20f
                paint.color = Color.BLACK
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas1.drawText("2. AI-STRUCTURED DAILY MEAL SCHEDULE", 36f, y, paint)

                y += 12f
                dietPlan.meals.forEachIndexed { index, meal ->
                    paint.color = if (index % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                    canvas1.drawRoundRect(RectF(36f, y, 559f, y + 32f), 4f, 4f, paint)

                    // Left accent dot
                    paint.color = Color.parseColor("#0D5C3A")
                    canvas1.drawCircle(46f, y + 16f, 4f, paint)

                    paint.color = Color.parseColor("#0D5C3A")
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas1.drawText("${meal.time} — ${meal.name.uppercase()}", 56f, y + 13f, paint)

                    paint.color = Color.BLACK
                    paint.textSize = 8.5f
                    canvas1.drawText("${meal.calories} kcal  (P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fat}g)", 380f, y + 13f, paint)

                    paint.color = Color.DKGRAY
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.DEFAULT
                    val foodsClean = if (meal.foods.length > 85) meal.foods.substring(0, 82) + "..." else meal.foods
                    canvas1.drawText("Ingredients: $foodsClean", 56f, y + 26f, paint)

                    y += 36f
                }

                if (dietPlan.explanationText.isNotBlank()) {
                    y += 8f
                    paint.color = Color.parseColor("#F0F4F8")
                    canvas1.drawRoundRect(RectF(36f, y, 559f, y + 42f), 6f, 6f, paint)

                    paint.color = Color.parseColor("#0D5C3A")
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas1.drawText("AI DIETITIAN GUIDANCE & REASONING:", 44f, y + 14f, paint)

                    paint.color = Color.DKGRAY
                    paint.textSize = 8f
                    paint.typeface = Typeface.DEFAULT
                    val expClean = if (dietPlan.explanationText.length > 150) dietPlan.explanationText.substring(0, 147) + "..." else dietPlan.explanationText
                    canvas1.drawText(expClean, 44f, y + 28f, paint)
                }
            } else if (workoutPlan != null && dietPlan == null) {
                // Render Workout Plan on Page 1 if no diet plan
                y += 20f
                paint.color = Color.BLACK
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas1.drawText("PERIODIZED WORKOUT PROGRAM (${workoutPlan.daysPerWeek} DAYS/WEEK)", 36f, y, paint)

                y += 12f
                workoutPlan.routines.forEach { routine ->
                    paint.color = Color.parseColor("#F8FAFC")
                    canvas1.drawRoundRect(RectF(36f, y, 559f, y + 60f), 6f, 6f, paint)

                    paint.color = Color.parseColor("#0D5C3A")
                    canvas1.drawRect(RectF(36f, y, 40f, y + 60f), paint)

                    paint.color = Color.BLACK
                    paint.textSize = 9.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas1.drawText("${routine.dayName.uppercase()}: ${routine.title}", 48f, y + 16f, paint)

                    var exY = y + 28f
                    routine.exercises.take(3).forEach { ex ->
                        paint.color = Color.DKGRAY
                        paint.textSize = 8.5f
                        paint.typeface = Typeface.DEFAULT
                        canvas1.drawText("• ${ex.name} — ${ex.sets} sets x ${ex.reps} (${ex.rest} rest)  ${if (ex.notes.isNotBlank()) " | " + ex.notes else ""}", 48f, exY, paint)
                        exY += 11f
                    }
                    y += 66f
                }
            }
        } else {
            // EXECUTIVE BUSINESS ANALYTICS REPORT
            y += 20f
            paint.color = Color.parseColor("#0D5C3A")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText("EXECUTIVE GYM BUSINESS ANALYTICS & GROWTH DOSSIER", 36f, y, paint)

            y += 20f
            val cardWidth = 122f
            val cardGap = 12f

            drawMetricCard(canvas1, paint, 36f, y, cardWidth, 55f, "GROSS REVENUE", "₹38,400", "#0D5C3A")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap), y, cardWidth, 55f, "ACTIVE MEMBERS", "284 Members", "#1E88E5")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap) * 2, y, cardWidth, 55f, "PROJECTED EOM", "₹48,000", "#388E3C")
            drawMetricCard(canvas1, paint, 36f + (cardWidth + cardGap) * 3, y, cardWidth, 55f, "PENDING DUES", "₹3,200", "#D32F2F")

            y += 75f

            // AI STRATEGIC GROWTH RECOMMENDATIONS BOX
            paint.color = Color.parseColor("#F4F6F8")
            canvas1.drawRoundRect(RectF(36f, y, 559f, y + 140f), 8f, 8f, paint)

            paint.color = Color.parseColor("#0D5C3A")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText("GEMINI AI STRATEGIC GROWTH INSIGHTS", 48f, y + 22f, paint)

            y += 38f
            paint.textSize = 9.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.BLACK
            val insightText = aiInsight ?: "1. Launch Off-Peak Pass discounts to maximize morning attendance.\n2. Convert 18 high-attendance trial leads into annual memberships.\n3. Implement automated WhatsApp renewal reminders 7 days prior to expiry."

            insightText.lines().take(5).forEach { line ->
                canvas1.drawText("• $line", 48f, y, paint)
                y += 18f
            }
        }

        drawPageFooter(canvas1, paint)
        pdfDocument.finishPage(page1)

        // PAGE 2: WORKOUT PLAN (if both diet & workout plans exist)
        if (dietPlan != null && workoutPlan != null) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2 = page2.canvas

            drawPageHeader(canvas2, paint, gymName, "PAGE 2 OF 2")

            var y2 = 80f

            paint.color = Color.parseColor("#0D5C3A")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas2.drawText("PERIODIZED WORKOUT PROGRAM (${workoutPlan.daysPerWeek} DAYS / WEEK SPLIT)", 36f, y2, paint)

            y2 += 16f
            paint.color = Color.DKGRAY
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            canvas2.drawText("Member: $memberName   |   Target Goal: ${workoutPlan.goal}", 36f, y2, paint)

            y2 += 20f

            workoutPlan.routines.forEach { routine ->
                val cardHeight = 30f + (routine.exercises.size * 14f)
                paint.color = Color.parseColor("#F8FAFC")
                canvas2.drawRoundRect(RectF(36f, y2, 559f, y2 + cardHeight), 6f, 6f, paint)

                // Accent Bar
                paint.color = Color.parseColor("#0D5C3A")
                canvas2.drawRect(RectF(36f, y2, 40f, y2 + cardHeight), paint)

                paint.color = Color.BLACK
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas2.drawText("${routine.dayName.uppercase()}: ${routine.title.uppercase()}", 48f, y2 + 18f, paint)

                var exY = y2 + 32f
                routine.exercises.forEach { ex ->
                    paint.color = Color.parseColor("#0D5C3A")
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas2.drawText("• ${ex.name}", 48f, exY, paint)

                    paint.color = Color.BLACK
                    paint.typeface = Typeface.DEFAULT
                    canvas2.drawText("${ex.sets} sets × ${ex.reps}  (Rest: ${ex.rest})", 260f, exY, paint)

                    if (ex.notes.isNotBlank()) {
                        paint.color = Color.GRAY
                        val notesClean = if (ex.notes.length > 40) ex.notes.substring(0, 37) + "..." else ex.notes
                        canvas2.drawText("Tips: $notesClean", 430f, exY, paint)
                    }

                    exY += 14f
                }

                y2 += cardHeight + 12f
            }

            // Training Guidelines Box
            if (y2 < 720f) {
                paint.color = Color.parseColor("#F0F4F8")
                canvas2.drawRoundRect(RectF(36f, y2, 559f, y2 + 50f), 6f, 6f, paint)

                paint.color = Color.parseColor("#0D5C3A")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas2.drawText("COACH TRAINING ADVICE & PROGRESSIVE OVERLOAD:", 44f, y2 + 16f, paint)

                paint.color = Color.DKGRAY
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT
                canvas2.drawText("• Perform 5-10 min dynamic warm-up before every session. Track working weight weekly.", 44f, y2 + 30f, paint)
                canvas2.drawText("• Ensure 7-8 hours sleep for full neuromuscular recovery. Hydrate 500ml water intra-workout.", 44f, y2 + 42f, paint)
            }

            drawPageFooter(canvas2, paint)
            pdfDocument.finishPage(page2)
        }

        val fileName = if (dietPlan != null || workoutPlan != null) "GymAI_Plan_${memberName.replace(" ", "_")}.pdf" else "GymAI_Executive_Report.pdf"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        return file
    }

    private fun drawPageHeader(canvas: android.graphics.Canvas, paint: Paint, gymName: String, pageLabel: String) {
        // Top Banner
        paint.color = Color.parseColor("#0D5C3A")
        canvas.drawRect(0f, 0f, 595f, 36f, paint)

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GYMAI PRO • OFFICIAL GRAPHICAL DOSSIER", 20f, 23f, paint)
        canvas.drawText(pageLabel, 500f, 23f, paint)

        // Header - Club Info
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(gymName.uppercase(), 36f, 56f, paint)

        paint.color = Color.parseColor("#0D5C3A")
        paint.textSize = 10f
        canvas.drawText("VERIFIED REPORT", 470f, 56f, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Bandra West, Mumbai • Ph: +91 98765 43210 • Web: www.gymai.pro", 36f, 68f, paint)

        paint.color = Color.LTGRAY
        canvas.drawLine(36f, 72f, 559f, 72f, paint)
    }

    private fun drawPageFooter(canvas: android.graphics.Canvas, paint: Paint) {
        val y = 780f
        paint.color = Color.LTGRAY
        canvas.drawLine(36f, y, 559f, y, paint)

        paint.color = Color.BLACK
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Certified Head PT: Coach Marcus", 36f, y + 18f, paint)
        canvas.drawText("Verified by GymAI Pro Engine", 390f, y + 18f, paint)
    }

    private fun drawMetricCard(
        canvas: android.graphics.Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        colorHex: String
    ) {
        // Draw Card Background
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, paint)

        // Draw Left Accent Stripe
        paint.color = Color.parseColor(colorHex)
        canvas.drawRect(RectF(x, y, x + 4f, y + height), paint)

        // Title
        paint.color = Color.GRAY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 10f, y + 18f, paint)

        // Value
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 10f, y + 38f, paint)
    }

    fun generateInvoicePdfFileForInvoice(
        context: Context,
        invoice: com.example.data.model.GymInvoice,
        gymName: String = "Metro Fitness Club"
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header Deep Purple/Emerald Bar
        paint.color = Color.parseColor("#1C103B")
        canvas.drawRect(0f, 0f, 595f, 52f, paint)

        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TAX INVOICE & MEMBERSHIP RECEIPT", 24f, 32f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("ORIGINAL RECEIPT", 450f, 32f, paint)

        var y = 85f

        // Gym Title & Invoice Info
        paint.color = Color.parseColor("#1C103B")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(gymName.uppercase(), 36f, y, paint)

        paint.color = Color.parseColor("#6750A4")
        paint.textSize = 15f
        canvas.drawText(invoice.id, 430f, y, paint)

        y += 20f
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Bandra West, Mumbai • Phone: +91 98765 43210 • GSTIN: 27AABCG1234H1Z5", 36f, y, paint)
        canvas.drawText("Date: ${invoice.date}", 430f, y, paint)

        y += 20f
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawLine(36f, y, 559f, y, paint)

        y += 24f
        // Member Details Card Box
        paint.color = Color.parseColor("#F7F2FA")
        canvas.drawRoundRect(RectF(36f, y, 559f, y + 72f), 8f, 8f, paint)

        paint.color = Color.parseColor("#6750A4")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BILLED TO MEMBER:", 48f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 13f
        canvas.drawText(invoice.memberName, 48f, y + 42f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Phone: ${invoice.memberPhone.ifBlank { "+91 98765 43210" }}   |   Issued By: ${invoice.createdByRole}", 48f, y + 58f, paint)

        y += 92f

        // Table Header Bar
        paint.color = Color.parseColor("#1C103B")
        canvas.drawRect(36f, y, 559f, y + 28f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ITEM / PACKAGE DESCRIPTION", 48f, y + 18f, paint)
        canvas.drawText("BASE PRICE", 310f, y + 18f, paint)
        canvas.drawText("DISCOUNT", 400f, y + 18f, paint)
        canvas.drawText("TAX (18%)", 480f, y + 18f, paint)

        y += 28f

        // Table Data Row
        paint.color = Color.WHITE
        canvas.drawRect(36f, y, 559f, y + 40f, paint)

        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.packageName, 48f, y + 24f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("₹${String.format("%.2f", invoice.amount)}", 310f, y + 24f, paint)
        canvas.drawText("₹${String.format("%.2f", invoice.discount)}", 400f, y + 24f, paint)
        canvas.drawText("₹${String.format("%.2f", invoice.taxAmount)}", 480f, y + 24f, paint)

        y += 40f
        paint.color = Color.LTGRAY
        canvas.drawLine(36f, y, 559f, y, paint)

        y += 24f

        // Payment Mode & Status Box
        val isPaid = invoice.paymentStatus.equals("Paid", ignoreCase = true)
        if (isPaid) {
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRoundRect(RectF(36f, y, 220f, y + 42f), 6f, 6f, paint)
            paint.color = Color.parseColor("#2E7D32")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("STATUS: PAID IN FULL", 48f, y + 26f, paint)
        } else {
            paint.color = Color.parseColor("#FFF3E0")
            canvas.drawRoundRect(RectF(36f, y, 220f, y + 42f), 6f, 6f, paint)
            paint.color = Color.parseColor("#E65100")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("STATUS: PAYMENT PENDING", 48f, y + 26f, paint)
        }

        // Summary Totals
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Payment Mode: ${invoice.paymentMode}", 330f, y + 16f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1C103B")
        canvas.drawText("GRAND TOTAL: ₹${String.format("%.2f", invoice.totalAmount)}", 330f, y + 38f, paint)

        y += 75f

        if (invoice.notes.isNotBlank()) {
            paint.color = Color.DKGRAY
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Notes: ${invoice.notes}", 36f, y, paint)
            y += 20f
        }

        // Terms & Footer
        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• This is an official computer-generated tax invoice. No signature is required.", 36f, y, paint)
        y += 14f
        canvas.drawText("• Fees once paid are non-refundable & non-transferable as per club terms.", 36f, y, paint)
        y += 14f
        canvas.drawText("• Thank you for choosing $gymName!", 36f, y, paint)

        pdfDocument.finishPage(page)

        val outputDir = context.cacheDir
        val file = File(outputDir, "invoice_${invoice.id}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun generateInvoicePdfFile(
        context: Context,
        member: Member,
        gymName: String = "Metro Fitness Club"
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Top Accent Banner (Emerald)
        paint.color = Color.parseColor("#0D5C3A")
        canvas.drawRect(0f, 0f, 595f, 44f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TAX INVOICE & MEMBERSHIP RECEIPT", 20f, 28f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("ORIGINAL FOR RECIPIENT", 440f, 28f, paint)

        var y = 75f

        // Gym Info
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(gymName.uppercase(), 36f, y, paint)

        paint.color = Color.parseColor("#0D5C3A")
        paint.textSize = 14f
        val invoiceNo = "INV-${member.id.take(8).uppercase()}"
        canvas.drawText(invoiceNo, 440f, y, paint)

        y += 18f
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Bandra West, Mumbai • Phone: +91 98765 43210 • Email: billing@gymai.pro", 36f, y, paint)

        paint.textSize = 9f
        canvas.drawText("Date: ${member.joiningDate}", 440f, y, paint)

        y += 18f
        canvas.drawLine(36f, y, 559f, y, paint)

        y += 24f
        // Billed To Section Card
        paint.color = Color.parseColor("#F4F6F8")
        canvas.drawRoundRect(RectF(36f, y, 559f, y + 68f), 8f, 8f, paint)

        paint.color = Color.parseColor("#0D5C3A")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEMBER / BILLED TO:", 48f, y + 20f, paint)

        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(member.name, 48f, y + 38f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Mobile: ${member.phone}   |   Email: ${member.email}", 48f, y + 54f, paint)

        y += 85f

        // Table Header
        paint.color = Color.parseColor("#0D5C3A")
        canvas.drawRect(36f, y, 559f, y + 26f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESCRIPTION / PLAN", 48f, y + 17f, paint)
        canvas.drawText("EXPIRY DATE", 320f, y + 17f, paint)
        canvas.drawText("AMOUNT (INR)", 460f, y + 17f, paint)

        y += 26f

        // Table Item Row
        paint.color = Color.WHITE
        canvas.drawRect(36f, y, 559f, y + 36f, paint)

        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(member.membershipPlan, 48f, y + 22f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(member.expiryDate, 320f, y + 22f, paint)

        val priceStr = "₹5,999.00"
        canvas.drawText(priceStr, 460f, y + 22f, paint)

        y += 36f
        paint.color = Color.LTGRAY
        canvas.drawLine(36f, y, 559f, y, paint)

        y += 20f

        // Status Card
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawRoundRect(RectF(36f, y, 200f, y + 36f), 6f, 6f, paint)
        paint.color = Color.parseColor("#2E7D32")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STATUS: ${member.paymentStatus.uppercase()}", 48f, y + 22f, paint)

        // Total Amount Summary
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL PAID:  ₹5,999.00", 380f, y + 22f, paint)

        y += 60f

        // Footer / Terms
        paint.color = Color.DKGRAY
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• This is a computer generated invoice and does not require physical signature.", 36f, y, paint)
        y += 14f
        canvas.drawText("• Membership dues are non-refundable & non-transferable under standard club policies.", 36f, y, paint)
        y += 14f
        canvas.drawText("• Powered by GymAI Pro Operating System", 36f, y, paint)

        pdfDocument.finishPage(page)

        val outputDir = context.cacheDir
        val file = File(outputDir, "invoice_${member.id.take(8)}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun generateAndSharePdfReport(
        context: Context,
        dietPlan: DietPlan?,
        workoutPlan: WorkoutPlan?,
        analytics: AnalyticsSummary? = null,
        aiInsight: String? = null,
        gymName: String = "Metro Fitness Club"
    ) {
        try {
            val file = generatePdfReportFile(context, dietPlan, workoutPlan, analytics, aiInsight, gymName)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GymAI Pro Official Report")
                putExtra(Intent.EXTRA_TEXT, "Attached is the official GymAI Pro graphical PDF report.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Graphical PDF Report"))
            Toast.makeText(context, "Official Graphical PDF generated!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Report export error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
