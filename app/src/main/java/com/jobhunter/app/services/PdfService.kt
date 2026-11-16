package com.jobhunter.app.services

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Environment
import com.jobhunter.app.models.UserProfile
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
        private const val PAGE_HEIGHT = 842 // A4 height in points (72 dpi)
        private const val MARGIN_LEFT = 40f
        private const val MARGIN_RIGHT = 40f
        private const val MARGIN_TOP = 40f
    }

    /**
     * Generate a professional CV/Resume PDF
     */
    suspend fun generateCvPdf(profile: UserProfile): File = withContext(Dispatchers.IO) {
        logger.i("PDF", "CV PDF oluşturuluyor: ${profile.firstName} ${profile.lastName}")
        val startTime = System.currentTimeMillis()
        val document = PdfDocument()
        
        try {

        // Paint objects for text styling
        val titlePaint = Paint().apply {
            textSize = 24f
            color = android.graphics.Color.parseColor("#1565C0")
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            textSize = 16f
            color = android.graphics.Color.parseColor("#1565C0")
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.parseColor("#666666")
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.parseColor("#333333")
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.parseColor("#888888")
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        val accentPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            strokeWidth = 2f
        }

        var pageNumber = 1
        var pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN_TOP

        // ===== HEADER SECTION =====
        // Full name
        val fullName = "${profile.firstName} ${profile.lastName}".trim()
        if (fullName.isNotEmpty()) {
            canvas.drawText(fullName, MARGIN_LEFT, y + 24f, titlePaint)
            y += 35f
        }

        // Contact info line
        val contactInfo = mutableListOf<String>()
        if (profile.email.isNotEmpty()) contactInfo.add(profile.email)
        if (profile.phone.isNotEmpty()) contactInfo.add(profile.phone)
        if (profile.location.isNotEmpty()) contactInfo.add(profile.location)

        if (contactInfo.isNotEmpty()) {
            canvas.drawText(contactInfo.joinToString("  |  "), MARGIN_LEFT, y + 14f, subtitlePaint)
            y += 20f
        }

        // Social links
        val links = mutableListOf<String>()
        if (profile.linkedinUrl.isNotEmpty()) links.add("LinkedIn: ${profile.linkedinUrl}")
        if (profile.githubUrl.isNotEmpty()) links.add("GitHub: ${profile.githubUrl}")
        if (profile.portfolioUrl.isNotEmpty()) links.add("Portfolio: ${profile.portfolioUrl}")

        if (links.isNotEmpty()) {
            canvas.drawText(links.joinToString("  |  "), MARGIN_LEFT, y + 14f, smallPaint)
            y += 18f
        }

        // Separator line
        y += 10f
        canvas.drawLine(MARGIN_LEFT, y, (PAGE_WIDTH - MARGIN_RIGHT).toFloat(), y, accentPaint)
        y += 15f

        // ===== SUMMARY SECTION =====
        if (profile.summary.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 150) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("ÖZET", MARGIN_LEFT, y + 14f, headerPaint)
            y += 20f

            y = drawWrappedText(canvas, profile.summary, MARGIN_LEFT, y, bodyPaint, PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT)
            y += 15f
        }

        // ===== EXPERIENCE SECTION =====
        if (profile.experiences.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 150) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("DENEYİMLER", MARGIN_LEFT, y + 14f, headerPaint)
            y += 20f

            for (exp in profile.experiences) {
                // Check if we need a new page
                if (y > PAGE_HEIGHT - 120) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN_TOP
                }

                // Position and company
                val posText = if (exp.position.isNotEmpty() && exp.company.isNotEmpty()) {
                    "${exp.position} - ${exp.company}"
                } else {
                    exp.position.ifEmpty { exp.company }
                }
                canvas.drawText(posText, MARGIN_LEFT, y + 12f, bodyPaint.apply { isFakeBoldText = true })

                // Date range
                val dateRange = "${exp.startDate} - ${exp.endDate.ifEmpty { "Devam Ediyor" }}"
                canvas.drawText(dateRange, MARGIN_LEFT, y + 26f, smallPaint)

                // Location
                if (exp.location.isNotEmpty()) {
                    val dateWidth = smallPaint.measureText(dateRange)
                    canvas.drawText(exp.location, MARGIN_LEFT + dateWidth + 20f, y + 26f, smallPaint)
                }

                y += 32f

                // Description
                if (exp.description.isNotEmpty()) {
                    y = drawWrappedText(canvas, exp.description, MARGIN_LEFT + 10f, y, bodyPaint, PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT - 10f)
                    y += 5f
                }

                // Separator
                y += 8f
                canvas.drawLine(MARGIN_LEFT, y, (PAGE_WIDTH - MARGIN_RIGHT).toFloat(), y, linePaint)
                y += 10f
            }

            y += 5f
        }

        // ===== EDUCATION SECTION =====
        if (profile.educations.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 150) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("EĞİTİM", MARGIN_LEFT, y + 14f, headerPaint)
            y += 20f

            for (edu in profile.educations) {
                // Check if we need a new page
                if (y > PAGE_HEIGHT - 100) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN_TOP
                }

                // School
                canvas.drawText(edu.school, MARGIN_LEFT, y + 12f, bodyPaint.apply { isFakeBoldText = true })

                // Department and degree
                val deptText = "${edu.department} - ${edu.degree}"
                canvas.drawText(deptText, MARGIN_LEFT, y + 26f, subtitlePaint)

                // Dates
                val dateRange = "${edu.startDate} - ${edu.endDate}"
                canvas.drawText(dateRange, MARGIN_LEFT, y + 40f, smallPaint)

                y += 50f

                // Separator
                canvas.drawLine(MARGIN_LEFT, y, (PAGE_WIDTH - MARGIN_RIGHT).toFloat(), y, linePaint)
                y += 10f
            }

            y += 5f
        }

        // ===== SKILLS SECTION =====
        if (profile.skills.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 100) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("YETENEKLER", MARGIN_LEFT, y + 14f, headerPaint)
            y += 22f

            // Draw skills as chips/tags
            var x = MARGIN_LEFT
            val chipPaint = Paint().apply {
                textSize = 10f
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }
            val chipBgPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1565C0")
                isAntiAlias = true
            }
            val chipHeight = 22f

            for (skill in profile.skills) {
                val chipWidth = chipPaint.measureText(skill) + 20f

                // Check if we need a new line
                if (x + chipWidth > PAGE_WIDTH - MARGIN_RIGHT) {
                    x = MARGIN_LEFT
                    y += chipHeight + 8f

                    // Check if we need a new page
                    if (y > PAGE_HEIGHT - 50) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN_TOP
                    }
                }

                // Draw chip background (rounded rect)
                canvas.drawRoundRect(
                    x, y, x + chipWidth, y + chipHeight,
                    4f, 4f, chipBgPaint
                )

                // Draw chip text
                canvas.drawText(skill, x + 10f, y + 16f, chipPaint)

                x += chipWidth + 8f
            }

            y += chipHeight + 15f
        }

        // ===== LANGUAGES SECTION =====
        if (profile.languages.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 80) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("DİLLER", MARGIN_LEFT, y + 14f, headerPaint)
            y += 20f

            canvas.drawText(profile.languages.joinToString("  •  "), MARGIN_LEFT, y + 12f, bodyPaint)
            y += 20f
        }

        // ===== CERTIFICATIONS SECTION =====
        if (profile.certifications.isNotEmpty()) {
            // Check if we need a new page
            if (y > PAGE_HEIGHT - 80) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }

            canvas.drawText("SERTİFİKALAR", MARGIN_LEFT, y + 14f, headerPaint)
            y += 20f

            for (cert in profile.certifications) {
                canvas.drawText("• $cert", MARGIN_LEFT, y + 12f, bodyPaint)
                y += 18f
            }
        }

        // Finish the last page
        document.finishPage(page)

        // Save to file
        val fileName = "CV_${profile.firstName}_${profile.lastName}.pdf".replace(" ", "_")
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir // Fallback to internal files dir
        val file = File(documentsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }

        val duration = System.currentTimeMillis() - startTime
        logger.logPdfGeneration(file.name, true, file.length())
        logger.d("PDF", "PDF oluşturuldu: ${file.name} (${file.length()} bytes, ${duration}ms)")
        
        file
        } finally {
            document.close()
        }
    }

    /**
     * Draw text that wraps to multiple lines
     * Returns the new Y position after drawing
     */
    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Float
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val lineWidth = paint.measureText(testLine)

            if (lineWidth > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, y + 12f, paint)
                y += 16f
                currentLine = word
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, y + 12f, paint)
            y += 16f
        }

        return y
    }

    /**
     * Get the generated PDF file path
     */
    fun getCvFilePath(profile: UserProfile): String {
        val fileName = "CV_${profile.firstName}_${profile.lastName}.pdf".replace(" ", "_")
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        ).absolutePath
    }
}
