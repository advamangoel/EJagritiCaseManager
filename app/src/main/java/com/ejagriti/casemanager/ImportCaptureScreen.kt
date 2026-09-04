package com.ejagriti.casemanager

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejagriti.casemanager.data.CaseEntity
import com.ejagriti.casemanager.importer.CaseImportEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ImportCaptureScreen(
    viewModel: CaseViewModel,
    existingCases: List<CaseEntity>,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var importedCases by remember {
        mutableStateOf<List<CaseEntity>>(emptyList())
    }

    var warnings by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var sourceName by remember {
        mutableStateOf("")
    }

    var processing by remember {
        mutableStateOf(false)
    }

    var showConfirm by remember {
        mutableStateOf(false)
    }

    var resultMessage by remember {
        mutableStateOf("")
    }

    val pdfLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            if (uri != null) {

                processing = true
                resultMessage = ""

                CoroutineScope(Dispatchers.Main).launch {

                    val result =
                        CaseImportEngine.importPdf(
                            context,
                            uri
                        )

                    importedCases =
                        result.cases

                    warnings =
                        result.warnings

                    sourceName =
                        result.sourceName

                    processing = false
                }
            }
        }

    val excelLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            if (uri != null) {

                processing = true
                resultMessage = ""

                CoroutineScope(Dispatchers.Main).launch {

                    val result =
                        CaseImportEngine.importExcel(
                            context,
                            uri
                        )

                    importedCases =
                        result.cases

                    warnings =
                        result.warnings

                    sourceName =
                        result.sourceName

                    processing = false
                }
            }
        }

    val duplicates =
        importedCases.filter { imported ->

            existingCases.any { existing ->

                (
                    imported.litigationId.isNotBlank() &&
                    existing.litigationId.equals(
                        imported.litigationId,
                        ignoreCase = true
                    )
                ) ||

                (
                    imported.newCaseNumber.isNotBlank() &&
                    existing.newCaseNumber.equals(
                        imported.newCaseNumber,
                        ignoreCase = true
                    )
                ) ||

                (
                    imported.oldCaseNumber.isNotBlank() &&
                    existing.oldCaseNumber.equals(
                        imported.oldCaseNumber,
                        ignoreCase = true
                    )
                )
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Import & Capture",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14213D)
                )
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Import Case Data",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            "Upload your standard Excel sheet or a scanned e-Jagriti PDF. The app will extract the cases and show them for review before saving."
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled = !processing,

                        onClick = {
                            excelLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel"
                                )
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.Description,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.padding(4.dp)
                        )

                        Text("Import Excel")
                    }

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled = !processing,

                        onClick = {
                            pdfLauncher.launch(
                                arrayOf(
                                    "application/pdf"
                                )
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.padding(4.dp)
                        )

                        Text("Scan / Import PDF")
                    }
                }
            }
        }

        if (processing) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            "Processing $sourceName...",
                        modifier =
                            Modifier.padding(20.dp),
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        if (sourceName.isNotBlank() &&
            !processing
        ) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            "Import Preview",
                            fontSize = 19.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            sourceName,
                            color = Color.Gray
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            "Extracted: ${importedCases.size}",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            "Possible duplicates: ${duplicates.size}",
                            color =
                                if (
                                    duplicates.isEmpty()
                                ) {
                                    Color(0xFF15803D)
                                } else {
                                    Color(0xFFD97706)
                                }
                        )
                    }
                }
            }
        }

        if (warnings.isNotEmpty()) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(0xFFFFF7ED)
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            "Warnings",
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                Color(0xFF9A3412)
                        )

                        warnings.take(10)
                            .forEach {

                                Text(
                                    "• $it",
                                    fontSize = 13.sp
                                )
                            }
                    }
                }
            }
        }

        if (importedCases.isNotEmpty()) {

            item {

                Text(
                    "Cases to be imported",
                    fontSize = 19.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF14213D)
                )
            }

            items(
                importedCases
            ) { caseItem ->

                ImportCaseCard(
                    caseItem = caseItem,
                    isDuplicate =
                        duplicates.any {
                            it.newCaseNumber ==
                                    caseItem.newCaseNumber &&
                                    caseItem.newCaseNumber.isNotBlank()
                        }
                )
            }

            item {

                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick = {
                        showConfirm = true
                    }
                ) {

                    Text(
                        "Review & Save ${importedCases.size} Cases"
                    )
                }
            }
        }

        if (resultMessage.isNotBlank()) {

            item {

                Text(
                    resultMessage,
                    color = Color(0xFF15803D),
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }

    if (showConfirm) {

        AlertDialog(

            onDismissRequest = {
                showConfirm = false
            },

            title = {
                Text("Confirm Import")
            },

            text = {
                Text(
                    "Save ${importedCases.size} extracted cases to the database? Possible duplicates are included only after your confirmation."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.importCases(
                            importedCases
                        ) { count ->

                            resultMessage =
                                "$count cases imported successfully."

                            importedCases =
                                emptyList()

                            warnings =
                                emptyList()

                            showConfirm =
                                false
                        }
                    }
                ) {
                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showConfirm = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ImportCaseCard(
    caseItem: CaseEntity,
    isDuplicate: Boolean
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    caseItem.newCaseNumber.ifBlank {
                        "Case number not identified"
                    },
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    Color(0xFF14213D)
            )

            if (
                isDuplicate
            ) {

                Text(
                    "⚠ Possible duplicate",
                    color =
                        Color(0xFFD97706),
                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                caseItem.partyName.isNotBlank()
            ) {

                Text(
                    caseItem.partyName,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            if (
                caseItem.nextHearingDate.isNotBlank()
            ) {

                Text(
                    "Hearing: ${caseItem.nextHearingDate}",
                    color =
                        Color(0xFF1D4ED8)
                )
            }

            Text(
                "Litigation ID: ${
                    caseItem.litigationId.ifBlank {
                        "Not identified"
                    }
                }",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}