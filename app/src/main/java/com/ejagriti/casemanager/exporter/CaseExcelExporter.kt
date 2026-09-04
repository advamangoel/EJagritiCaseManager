package com.ejagriti.casemanager.exporter

import com.ejagriti.casemanager.data.CaseEntity
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CaseExcelExporter {

    fun createWorkbook(
        cases: List<CaseEntity>,
        selectedColumns: List<String>
    ): ByteArray {

        require(selectedColumns.isNotEmpty()) {
            "At least one column must be selected."
        }

        val workbookXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                      xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets>
                    <sheet name="Cases" sheetId="1" r:id="rId1"/>
                </sheets>
            </workbook>
        """.trimIndent()

        val workbookRelsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                    Target="worksheets/sheet1.xml"/>
            </Relationships>
        """.trimIndent()

        val sheetXml = buildSheetXml(
            cases = cases,
            selectedColumns = selectedColumns
        )

        val contentTypesXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels"
                    ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/xl/workbook.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/worksheets/sheet1.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            </Types>
        """.trimIndent()

        val rootRelsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="xl/workbook.xml"/>
            </Relationships>
        """.trimIndent()

        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { zip ->

            addEntry(
                zip,
                "[Content_Types].xml",
                contentTypesXml
            )

            addEntry(
                zip,
                "_rels/.rels",
                rootRelsXml
            )

            addEntry(
                zip,
                "xl/workbook.xml",
                workbookXml
            )

            addEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                workbookRelsXml
            )

            addEntry(
                zip,
                "xl/worksheets/sheet1.xml",
                sheetXml
            )
        }

        return output.toByteArray()
    }

    private fun buildSheetXml(
        cases: List<CaseEntity>,
        selectedColumns: List<String>
    ): String {

        val labels = mapOf(
            "litigationId" to "Litigation ID",
            "newCaseNumber" to "New Case Number",
            "oldCaseNumber" to "Old Case Number",
            "partyName" to "Complainant / Party",
            "oppositeParty" to "Opposite Party",
            "courtCommission" to "Commission",
            "caseType" to "Case Type",
            "state" to "State",
            "district" to "District",
            "nextHearingDate" to "Next Hearing Date",
            "caseStatus" to "Case Status"
        )

        val xml = StringBuilder()

        xml.append(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>"""
        )

        xml.append(
            """<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">"""
        )

        xml.append("<sheetData>")

        xml.append("<row r=\"1\">")

        selectedColumns.forEachIndexed { index, key ->
            val label = labels[key] ?: key
            appendInlineStringCell(
                xml = xml,
                reference = "${columnName(index + 1)}1",
                value = label
            )
        }

        xml.append("</row>")

        cases.forEachIndexed { rowIndex, caseItem ->

            val rowNumber = rowIndex + 2

            xml.append(
                """<row r="$rowNumber">"""
            )

            selectedColumns.forEachIndexed { columnIndex, key ->

                val value = when (key) {
                    "litigationId" -> caseItem.litigationId
                    "newCaseNumber" -> caseItem.newCaseNumber
                    "oldCaseNumber" -> caseItem.oldCaseNumber
                    "partyName" -> caseItem.partyName
                    "oppositeParty" -> caseItem.oppositeParty
                    "courtCommission" -> caseItem.courtCommission
                    "caseType" -> caseItem.caseType
                    "state" -> caseItem.state
                    "district" -> caseItem.district
                    "nextHearingDate" -> caseItem.nextHearingDate
                    "caseStatus" -> caseItem.caseStatus
                    else -> ""
                }

                appendInlineStringCell(
                    xml = xml,
                    reference =
                        "${columnName(columnIndex + 1)}$rowNumber",
                    value = value
                )
            }

            xml.append("</row>")
        }

        xml.append("</sheetData>")
        xml.append("</worksheet>")

        return xml.toString()
    }

    private fun appendInlineStringCell(
        xml: StringBuilder,
        reference: String,
        value: String
    ) {

        xml.append(
            """<c r="$reference" t="inlineStr"><is><t>${escapeXml(value)}</t></is></c>"""
        )
    }

    private fun escapeXml(value: String): String {

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .replace("\r", " ")
            .replace("\n", " ")
    }

    private fun columnName(number: Int): String {

        var n = number
        val result = StringBuilder()

        while (n > 0) {
            val remainder = (n - 1) % 26
            result.append(('A'.code + remainder).toChar())
            n = (n - 1) / 26
        }

        return result.reverse().toString()
    }

    private fun addEntry(
        zip: ZipOutputStream,
        path: String,
        content: String
    ) {

        zip.putNextEntry(ZipEntry(path))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
