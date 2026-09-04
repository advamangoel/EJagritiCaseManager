package com.ejagriti.casemanager.importer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.ejagriti.casemanager.data.CaseEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ImportResult(
    val cases: List<CaseEntity>,
    val warnings: List<String>,
    val sourceName: String
)

object CaseImportEngine {

    suspend fun importExcel(
        context: Context,
        uri: Uri
    ): ImportResult = withContext(Dispatchers.IO) {

        val sourceName =
            getFileName(context, uri) ?: "Excel file"

        val sharedStrings =
            readSharedStrings(context, uri)

        val sheetXml =
            readFirstWorksheet(context, uri)

        if (sheetXml.isBlank()) {
            return@withContext ImportResult(
                emptyList(),
                listOf("Could not find the first worksheet."),
                sourceName
            )
        }

        val rows =
            parseSheetRows(
                sheetXml,
                sharedStrings
            )

        val cases = mutableListOf<CaseEntity>()
        val warnings = mutableListOf<String>()

        rows.drop(1).forEachIndexed { index, row ->

            if (row.all { it.isBlank() }) {
                return@forEachIndexed
            }

            val values =
                (row + List(11) { "" }).take(11)

            val litigationId =
                values[0].trim()

            val newCaseNumber =
                values[1].trim()

            val oldCaseNumber =
                values[2].trim()

            val partyName =
                values[3].trim()

            val oppositeParty =
                values[4].trim()

            val commission =
                values[5].trim()
                    .ifBlank {
                        "District Consumer Commission"
                    }

            val caseType =
                values[6].trim()
                    .ifBlank {
                        "Consumer Complaint"
                    }

            val state =
                values[7].trim()
                    .ifBlank {
                        "Maharashtra"
                    }

            val district =
                values[8].trim()

            val hearingDate =
                normalizeDate(values[9].trim())

            val status =
                values[10].trim()
                    .ifBlank {
                        "Pending"
                    }

            if (
                litigationId.isBlank() &&
                newCaseNumber.isBlank() &&
                oldCaseNumber.isBlank()
            ) {
                warnings.add(
                    "Row ${index + 2}: no Litigation ID or case number."
                )

                return@forEachIndexed
            }

            if (litigationId.isBlank()) {
                warnings.add(
                    "Row ${index + 2}: Litigation ID is blank."
                )
            }

            cases.add(
                CaseEntity(
                    litigationId = litigationId,
                    newCaseNumber = newCaseNumber,
                    oldCaseNumber = oldCaseNumber,
                    partyName = partyName,
                    oppositeParty = oppositeParty,
                    courtCommission = commission,
                    caseType = caseType,
                    state = state,
                    district = district,
                    nextHearingDate = hearingDate,
                    caseStatus = status
                )
            )
        }

        ImportResult(
            cases = cases,
            warnings = warnings,
            sourceName = sourceName
        )
    }

    suspend fun importPdf(
        context: Context,
        uri: Uri
    ): ImportResult = withContext(Dispatchers.IO) {

        val sourceName =
            getFileName(context, uri) ?: "PDF file"

        val warnings = mutableListOf<String>()
        val extractedText = StringBuilder()

        val descriptor =
            context.contentResolver.openFileDescriptor(
                uri,
                "r"
            )

        if (descriptor == null) {
            return@withContext ImportResult(
                emptyList(),
                listOf("Unable to open PDF."),
                sourceName
            )
        }

        descriptor.use { parcelFileDescriptor ->

            val renderer =
                PdfRenderer(parcelFileDescriptor)

            try {

                for (
                    pageNumber in 0 until renderer.pageCount
                ) {

                    val page =
                        renderer.openPage(pageNumber)

                    try {

                        val width =
                            page.width.coerceAtMost(1800)

                        val scale =
                            width.toFloat() /
                                    page.width.toFloat()

                        val height =
                            (page.height * scale)
                                .toInt()
                                .coerceAtLeast(1)

                        val bitmap =
                            Bitmap.createBitmap(
                                width,
                                height,
                                Bitmap.Config.ARGB_8888
                            )

                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        val text =
                            recognizeBitmap(bitmap)

                        extractedText
                            .append("\n")
                            .append(text)

                        bitmap.recycle()

                    } finally {
                        page.close()
                    }
                }

            } finally {
                renderer.close()
            }
        }

        val text =
            extractedText.toString()

        if (text.isBlank()) {
            warnings.add(
                "OCR did not find readable text in the PDF."
            )
        }

        val cases =
            extractCasesFromText(text, warnings)

        ImportResult(
            cases = cases,
            warnings = warnings,
            sourceName = sourceName
        )
    }

    private suspend fun recognizeBitmap(
        bitmap: Bitmap
    ): String = suspendCancellableCoroutine { continuation ->

        val image =
            InputImage.fromBitmap(
                bitmap,
                0
            )

        val recognizer =
            TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

        recognizer
            .process(image)
            .addOnSuccessListener { result ->

                if (continuation.isActive) {
                    continuation.resume(result.text)
                }

                recognizer.close()
            }
            .addOnFailureListener { exception ->

                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }

                recognizer.close()
            }

        continuation.invokeOnCancellation {
            recognizer.close()
        }
    }

    private fun extractCasesFromText(
        text: String,
        warnings: MutableList<String>
    ): List<CaseEntity> {

        val results =
            mutableListOf<CaseEntity>()

        val lines =
            text.lines()
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        val casePattern =
            Regex(
                """(?i)\b(CC|FA|RP|EA|RA|MA)\s*[/\-]?\s*(\d{1,6})\s*(?:[/\-]|OF)\s*(\d{4})\b"""
            )

        val hearingPattern =
            Regex(
                """\b(\d{1,2})[./-](\d{1,2})[./-](\d{2,4})\b"""
            )

        lines.forEachIndexed { index, line ->

            val match =
                casePattern.find(line)

            if (match != null) {

                val prefix =
                    match.groupValues[1]
                        .uppercase()

                val number =
                    match.groupValues[2]

                val year =
                    match.groupValues[3]

                val caseNumber =
                    "$prefix/$number/$year"

                val nearby =
                    lines.subList(
                        maxOf(0, index - 1),
                        minOf(
                            lines.size,
                            index + 3
                        )
                    )

                val combined =
                    nearby.joinToString(" ")

                val hearingMatch =
                    hearingPattern.find(combined)

                val hearingDate =
                    hearingMatch
                        ?.let {
                            normalizeDate(it.value)
                        }
                        ?: ""

                val party =
                    extractPartyName(
                        line,
                        caseNumber
                    )

                results.add(
                    CaseEntity(
                        litigationId = "",
                        newCaseNumber = caseNumber,
                        oldCaseNumber = "",
                        partyName = party,
                        oppositeParty = "",
                        courtCommission =
                            guessCommission(combined),
                        caseType =
                            guessCaseType(prefix),
                        state = "",
                        district = "",
                        nextHearingDate =
                            hearingDate,
                        caseStatus = "Pending"
                    )
                )
            }
        }

        if (results.isEmpty()) {
            warnings.add(
                "No recognizable case numbers were found. " +
                        "OCR text may need a different case-number pattern."
            )
        }

        return results.distinctBy {
            it.newCaseNumber
        }
    }

    private fun extractPartyName(
        line: String,
        caseNumber: String
    ): String {

        val cleaned =
            line.replace(
                caseNumber,
                "",
                ignoreCase = true
            )
                .replace(
                    Regex("""\s+"""),
                    " "
                )
                .trim(
                    ' ',
                    '-',
                    ':',
                    '|',
                    '/'
                )

        return if (
            cleaned.length in 3..120
        ) {
            cleaned
        } else {
            ""
        }
    }

    private fun guessCommission(
        text: String
    ): String {

        val lower =
            text.lowercase()

        return when {

            "national" in lower ||
                    "ncdrc" in lower ->
                "National Consumer Commission"

            "state" in lower ||
                    "scdrc" in lower ->
                "State Consumer Commission"

            else ->
                "District Consumer Commission"
        }
    }

    private fun guessCaseType(
        prefix: String
    ): String {

        return when (prefix) {

            "CC" ->
                "Consumer Complaint"

            "FA" ->
                "First Appeal"

            "RP" ->
                "Revision Petition"

            "EA" ->
                "Execution Application"

            "RA" ->
                "Review Application"

            else ->
                "Miscellaneous Application"
        }
    }

    private fun normalizeDate(
        value: String
    ): String {

        if (value.isBlank()) {
            return ""
        }

        val match =
            Regex(
                """(\d{1,2})[./-](\d{1,2})[./-](\d{2,4})"""
            ).find(value)

        if (match == null) {
            return value
        }

        val day =
            match.groupValues[1]
                .toIntOrNull()
                ?: return value

        val month =
            match.groupValues[2]
                .toIntOrNull()
                ?: return value

        var year =
            match.groupValues[3]
                .toIntOrNull()
                ?: return value

        if (year < 100) {
            year += 2000
        }

        return "%04d-%02d-%02d".format(
            year,
            month,
            day
        )
    }

    private fun readSharedStrings(
        context: Context,
        uri: Uri
    ): List<String> {

        val xml =
            readZipEntry(
                context,
                uri,
                "xl/sharedStrings.xml"
            )

        if (xml.isBlank()) {
            return emptyList()
        }

        val result =
            mutableListOf<String>()

        val parser =
            XmlPullParserFactory
                .newInstance()
                .newPullParser()

        parser.setInput(
            xml.reader()
        )

        var event =
            parser.eventType

        var current =
            StringBuilder()

        while (
            event != XmlPullParser.END_DOCUMENT
        ) {

            if (
                event ==
                XmlPullParser.START_TAG &&
                parser.name == "si"
            ) {
                current = StringBuilder()
            }

            if (
                event ==
                XmlPullParser.TEXT
            ) {
                current.append(
                    parser.text
                )
            }

            if (
                event ==
                XmlPullParser.END_TAG &&
                parser.name == "si"
            ) {
                result.add(
                    current.toString()
                )
            }

            event =
                parser.next()
        }

        return result
    }

    private fun readFirstWorksheet(
        context: Context,
        uri: Uri
    ): String {

        return readZipEntry(
            context,
            uri,
            "xl/worksheets/sheet1.xml"
        )
    }

    private fun readZipEntry(
        context: Context,
        uri: Uri,
        entryName: String
    ): String {

        val input =
            context.contentResolver
                .openInputStream(uri)
                ?: return ""

        input.use { stream ->

            ZipInputStream(stream)
                .use { zip ->

                    var entry =
                        zip.nextEntry

                    while (entry != null) {

                        if (
                            entry.name ==
                            entryName
                        ) {

                            return zip
                                .readBytes()
                                .toString(
                                    Charsets.UTF_8
                                )
                        }

                        entry =
                            zip.nextEntry
                    }
                }
        }

        return ""
    }

    private fun parseSheetRows(
        xml: String,
        sharedStrings: List<String>
    ): List<List<String>> {

        val rows =
            mutableListOf<List<String>>()

        val parser =
            XmlPullParserFactory
                .newInstance()
                .newPullParser()

        parser.setInput(
            xml.reader()
        )

        var event =
            parser.eventType

        var currentRow =
            mutableListOf<String>()

        var currentValue =
            ""

        var cellType =
            ""

        while (
            event != XmlPullParser.END_DOCUMENT
        ) {

            when (event) {

                XmlPullParser.START_TAG -> {

                    when (parser.name) {

                        "row" -> {
                            currentRow =
                                mutableListOf()
                        }

                        "c" -> {
                            cellType =
                                parser.getAttributeValue(
                                    null,
                                    "t"
                                ) ?: ""
                        }

                        "v" -> {
                            currentValue = ""
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    currentValue +=
                        parser.text
                }

                XmlPullParser.END_TAG -> {

                    when (parser.name) {

                        "v" -> {

                            var value =
                                currentValue

                            if (
                                cellType ==
                                "s"
                            ) {

                                val index =
                                    value
                                        .toIntOrNull()

                                if (
                                    index != null &&
                                    index in
                                    sharedStrings.indices
                                ) {

                                    value =
                                        sharedStrings[index]
                                }
                            }

                            currentRow.add(
                                value
                            )
                        }

                        "row" -> {
                            rows.add(
                                currentRow
                            )
                        }
                    }
                }
            }

            event =
                parser.next()
        }

        return rows
    }

    private fun getFileName(
        context: Context,
        uri: Uri
    ): String? {

        return try {

            context.contentResolver
                .query(
                    uri,
                    arrayOf("_display_name"),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->

                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else {
                        null
                    }
                }

        } catch (_: Exception) {
            ""
        }
    }
}