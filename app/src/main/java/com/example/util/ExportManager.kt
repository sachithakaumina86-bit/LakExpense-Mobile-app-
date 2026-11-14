package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.CategorySpendSummary
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DateFormatOption
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import java.io.File
import java.io.OutputStream

class ExportManager(private val context: Context) {

    fun writeCsv(
        outputStream: OutputStream,
        items: List<TransactionItem>,
        dateFormat: DateFormatOption,
        currency: CurrencyDisplayOption
    ) {
        outputStream.bufferedWriter().use { writer ->
            writer.appendLine("Date,Time,Type,Category,Merchant,Payment Method,To Account,Amount (${currency.symbol}),Note")
            for (item in items) {
                val tx = item.entity
                val date = DateUtils.formatDate(tx.timestamp, dateFormat)
                val time = DateUtils.formatTime(tx.timestamp)
                val category = escapeCsv(item.category?.name ?: "Other")
                val merchant = escapeCsv(tx.merchant.ifBlank { category })
                val method = escapeCsv(item.paymentMethod?.name ?: "Cash")
                val toMethod = escapeCsv(item.toPaymentMethod?.name ?: "")
                val signedCents = when (tx.type) {
                    TransactionType.EXPENSE -> -tx.amountCents
                    TransactionType.INCOME -> tx.amountCents
                    TransactionType.TRANSFER -> tx.amountCents
                }
                val amount = MoneyMath.formatNumberOnly(signedCents, includeDecimals = true)
                    .replace(",", "")
                val note = escapeCsv(tx.note)
                writer.appendLine("$date,$time,${tx.type.label},$category,$merchant,$method,$toMethod,$amount,$note")
            }
        }
    }

    fun writePdfStatement(
        outputStream: OutputStream,
        ownerName: String,
        periodLabel: String,
        totalInflowCents: Long,
        totalOutflowCents: Long,
        netCashFlowCents: Long,
        categoryBreakdown: List<CategorySpendSummary>,
        items: List<TransactionItem>,
        dateFormat: DateFormatOption,
        currency: CurrencyDisplayOption
    ) {
        val document = PdfDocument()
        val pageWidth = 595 // A4 width in PostScript points
        val pageHeight = 842
        val margin = 40f

        val headerBgPaint = Paint().apply { color = Color.rgb(12, 19, 33) }
        val accentPaint = Paint().apply { color = Color.rgb(16, 185, 129) }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(110, 231, 183)
            textSize = 11f
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isAntiAlias = true
        }
        val mutedPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }
        val cardPaint = Paint().apply { color = Color.rgb(248, 250, 252) }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Dark Emerald Vault Header Band
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 98f, headerBgPaint)
        canvas.drawRect(0f, 94f, pageWidth.toFloat(), 98f, accentPaint)
        canvas.drawText("LakExpense Financial Statement", margin, 42f, titlePaint)
        canvas.drawText("Every Rupee. Under Control. • $periodLabel", margin, 62f, subtitlePaint)
        canvas.drawText("Account Holder: $ownerName  |  Offline Local Vault", margin, 80f, Paint(subtitlePaint).apply {
            color = Color.rgb(203, 213, 225)
            textSize = 9.5f
        })

        var y = 122f

        // Summary box
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 64f, 8f, 8f, cardPaint)
        canvas.drawText("TOTAL INFLOW", margin + 14f, y + 22f, mutedPaint)
        canvas.drawText(
            MoneyMath.formatAmount(totalInflowCents, currency),
            margin + 14f,
            y + 42f,
            Paint(sectionPaint).apply { color = Color.rgb(5, 150, 105) }
        )

        canvas.drawText("TOTAL OUTFLOW", margin + 190f, y + 22f, mutedPaint)
        canvas.drawText(
            MoneyMath.formatAmount(totalOutflowCents, currency),
            margin + 190f,
            y + 42f,
            Paint(sectionPaint).apply { color = Color.rgb(220, 38, 38) }
        )

        canvas.drawText("NET CASH FLOW", margin + 368f, y + 22f, mutedPaint)
        canvas.drawText(
            MoneyMath.formatAmount(netCashFlowCents, currency, showSign = true),
            margin + 368f,
            y + 42f,
            sectionPaint
        )
        y += 88f

        // Category breakdown
        if (categoryBreakdown.isNotEmpty()) {
            canvas.drawText("Category Distribution", margin, y, sectionPaint)
            y += 16f
            for (cat in categoryBreakdown.take(8)) {
                canvas.drawText(cat.category.name, margin, y, bodyPaint)
                canvas.drawText(
                    "${cat.percentage}%",
                    pageWidth - margin - 130f,
                    y,
                    mutedPaint
                )
                canvas.drawText(
                    MoneyMath.formatAmount(cat.totalCents, currency),
                    pageWidth - margin - 80f,
                    y,
                    bodyPaint
                )
                y += 16f
            }
            y += 10f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 22f
        }

        // Transactions Ledger Header
        fun drawLedgerHeader(c: android.graphics.Canvas, topY: Float): Float {
            c.drawText("Transaction Ledger (${items.size} entries)", margin, topY, sectionPaint)
            val headerY = topY + 18f
            c.drawText("DATE", margin, headerY, mutedPaint)
            c.drawText("MERCHANT / NOTE", margin + 80f, headerY, mutedPaint)
            c.drawText("CATEGORY", margin + 275f, headerY, mutedPaint)
            c.drawText("METHOD", margin + 365f, headerY, mutedPaint)
            c.drawText("AMOUNT", pageWidth - margin - 65f, headerY, mutedPaint)
            c.drawLine(margin, headerY + 6f, pageWidth - margin, headerY + 6f, dividerPaint)
            return headerY + 20f
        }

        y = drawLedgerHeader(canvas, y)

        for (item in items) {
            if (y > pageHeight - 50f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = drawLedgerHeader(canvas, 48f)
            }
            val tx = item.entity
            val dateStr = DateUtils.formatDate(tx.timestamp, dateFormat)
            val merchantStr = tx.merchant.ifBlank { item.category?.name ?: tx.type.label }.take(28)
            val catStr = (item.category?.name ?: tx.type.label).take(14)
            val methodStr = (item.paymentMethod?.name ?: "Cash").take(13)
            val amountStr = when (tx.type) {
                TransactionType.EXPENSE -> MoneyMath.formatAmount(tx.amountCents, currency, isPositiveOverride = false)
                TransactionType.INCOME -> MoneyMath.formatAmount(tx.amountCents, currency, isPositiveOverride = true)
                TransactionType.TRANSFER -> MoneyMath.formatAmount(tx.amountCents, currency)
            }
            val amountPaint = Paint(bodyPaint).apply {
                color = when (tx.type) {
                    TransactionType.EXPENSE -> Color.rgb(220, 38, 38)
                    TransactionType.INCOME -> Color.rgb(5, 150, 105)
                    TransactionType.TRANSFER -> Color.rgb(37, 99, 235)
                }
            }

            canvas.drawText(dateStr, margin, y, bodyPaint)
            canvas.drawText(merchantStr, margin + 80f, y, bodyPaint)
            canvas.drawText(catStr, margin + 275f, y, mutedPaint)
            canvas.drawText(methodStr, margin + 365f, y, mutedPaint)
            canvas.drawText(amountStr, pageWidth - margin - 85f, y, amountPaint)
            y += 18f
        }

        document.finishPage(page)
        outputStream.use { document.writeTo(it) }
        document.close()
    }

    fun createShareableExportIntent(fileName: String, mimeType: String, writer: (OutputStream) -> Unit): Intent {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use(writer)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\"") || clean.contains("\n")) {
            "\"$clean\""
        } else {
            clean
        }
    }
}
