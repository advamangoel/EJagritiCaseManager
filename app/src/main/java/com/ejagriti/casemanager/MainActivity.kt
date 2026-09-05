package com.ejagriti.casemanager

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejagriti.casemanager.data.CaseEntity
import com.ejagriti.casemanager.exporter.CaseExcelExporter
import java.util.Calendar

private val Navy = Color(0xFF14213D)
private val Blue = Color(0xFF1D4ED8)
private val Orange = Color(0xFFD97706)
private val Green = Color(0xFF15803D)
private val LightBackground = Color(0xFFF6F7FB)

class MainActivity : ComponentActivity() {

    private val caseViewModel: CaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                EJagritiApp(caseViewModel)
            }
        }
    }
}

@Composable
fun EJagritiApp(viewModel: CaseViewModel) {

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddCase by remember { mutableStateOf(false) }
    var showImportCapture by remember { mutableStateOf(false) }
    var selectedCase by remember { mutableStateOf<CaseEntity?>(null) }

    val cases by viewModel.cases.collectAsState()

    if (selectedCase != null) {
        CaseDetailsScreen(
            caseItem = selectedCase!!,
            viewModel = viewModel,
            onBack = {
                selectedCase = null
            }
        )
        return
    }

    if (showImportCapture) {
        ImportCaptureScreen(
            viewModel = viewModel,
            existingCases = cases,
            onBack = {
                showImportCapture = false
            }
        )
        return
    }

    Scaffold(
        containerColor = LightBackground,

        floatingActionButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        showAddCase = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text("Add Case")
                }
            }
        },

        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Dashboard")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.WorkOutline,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Cases")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Hearings")
                    }
                )
            }
        }

    ) { paddingValues ->

        when (selectedTab) {

            0 -> DashboardScreen(
                cases = cases,
                onImportCapture = {
                    showImportCapture = true
                },
                modifier = Modifier.padding(paddingValues)
            )

            1 -> CasesScreen(
                cases = cases,
                viewModel = viewModel,
                onCaseClick = {
                    selectedCase = it
                },
                modifier = Modifier.padding(paddingValues)
            )

            2 -> HearingsScreen(
                cases = cases,
                onCaseClick = {
                    selectedCase = it
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (showAddCase) {

        CaseFormDialog(
            title = "Add New Case",
            initialCase = null,

            onDismiss = {
                showAddCase = false
            },

            onSave = { form ->

                viewModel.addCase(
                    litigationId = form.litigationId,
                    newCaseNumber = form.newCaseNumber,
                    oldCaseNumber = form.oldCaseNumber,
                    partyName = form.partyName,
                    oppositeParty = form.oppositeParty,
                    courtCommission = form.courtCommission,
                    caseType = form.caseType,
                    state = form.state,
                    district = form.district,
                    nextHearingDate = form.nextHearingDate,
                    caseStatus = form.caseStatus
                )

                showAddCase = false
            }
        )
    }
}

@Composable
fun DashboardScreen(
    cases: List<CaseEntity>,
    onImportCapture: () -> Unit,
    modifier: Modifier = Modifier
) {

    val today = getTodayDate()
    val tomorrow = getTomorrowDate()
    val currentMonth = getCurrentMonthPrefix()

    val todayCases = cases.filter {
        it.nextHearingDate == today
    }

    val tomorrowCases = cases.filter {
        it.nextHearingDate == tomorrow
    }

    val monthCases = cases.filter {
        it.nextHearingDate.startsWith(currentMonth)
    }

    val pendingCases = cases.count {
        it.caseStatus == "Pending"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Column {

                Text(
                    text = "e-Jagriti Case Manager",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Litigation & Consumer Case Management",
                    color = Color.Gray
                )
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Navy
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Text(
                        text = "TODAY'S HEARINGS",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todayCases.size.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = formatDisplayDate(today),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = tomorrowCases.size.toString(),
                    label = "Tomorrow",
                    color = Blue
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = monthCases.size.toString(),
                    label = "This Month",
                    color = Orange
                )
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = cases.size.toString(),
                    label = "Total Cases",
                    color = Navy
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = pendingCases.toString(),
                    label = "Pending",
                    color = Green
                )
            }
        }

        item {

            Text(
                text = "Today's Hearing List",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        if (todayCases.isEmpty()) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "No hearings scheduled for today.",
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Your hearing schedule is clear today.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        } else {

            items(
                items = todayCases,
                key = { it.id }
            ) { caseItem ->

                DashboardHearingCard(caseItem)
            }
        }

        item {

            Text(
                text = "Case Identification",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        item {
            InfoCard(
                title = "Litigation ID",
                description = "Organisation's unique litigation reference"
            )
        }

        item {
            InfoCard(
                title = "New Case Number",
                description = "Current e-Jagriti case number"
            )
        }

        item {
            InfoCard(
                title = "Old Case Number",
                description = "Legacy case reference for searching and mapping"
            )
        }

        item {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onImportCapture()
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Blue
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text(
                            text = "Import & Capture",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Import Excel files or scan PDF case documents using OCR.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "Excel  •  PDF / OCR  •  Review  •  Save",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHearingCard(
    caseItem: CaseEntity
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = caseItem.partyName.ifBlank {
                    "Unnamed Case"
                },
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(modifier = Modifier.height(8.dp))

            CaseDetail(
                "Litigation ID",
                caseItem.litigationId
            )

            CaseDetail(
                "Old Case No.",
                caseItem.oldCaseNumber
            )

            CaseDetail(
                "New Case No.",
                caseItem.newCaseNumber
            )

            CaseDetail(
                "Commission",
                caseItem.courtCommission
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    color: Color
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CasesScreen(
    cases: List<CaseEntity>,
    viewModel: CaseViewModel,
    onCaseClick: (CaseEntity) -> Unit,
    modifier: Modifier = Modifier
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var deleteCase by remember {
        mutableStateOf<CaseEntity?>(null)
    }

    var showExport by remember {
        mutableStateOf(false)
    }

    var pendingExportBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }

    val context = LocalContext.current

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        ) { uri: Uri? ->
            val bytes = pendingExportBytes

            if (uri != null && bytes != null) {
                try {
                    val output = context.contentResolver.openOutputStream(uri)

                    if (output != null) {
                        output.use { it.write(bytes) }

                        Toast.makeText(
                            context,
                            "Excel file exported successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Could not create the Excel file.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (exception: Exception) {
                    Toast.makeText(
                        context,
                        "Export failed: ${exception.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            pendingExportBytes = null
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text = "Cases",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        item {

            OutlinedTextField(
                value = searchText,

                onValueChange = {
                    searchText = it
                    viewModel.searchCases(it)
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Search Litigation ID / New / Old Case No.")
                },

                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },

                singleLine = true
            )
        }

        item {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showExport = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                )
            ) {

                Icon(
                    Icons.Default.Description,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text("Export to Excel")
            }
        }

        if (cases.isEmpty()) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "No cases found",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Tap Add Case to create your first case",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        } else {

            items(
                items = cases,
                key = { it.id }
            ) { caseItem ->

                CaseCard(
                    caseItem = caseItem,

                    onClick = {
                        onCaseClick(caseItem)
                    },

                    onDelete = {
                        deleteCase = caseItem
                    }
                )
            }
        }
    }

    if (showExport) {

        ExportColumnDialog(
            onDismiss = {
                showExport = false
            },
            onExport = { selectedColumns ->

                try {
                    pendingExportBytes =
                        CaseExcelExporter
                            .createWorkbook(
                                cases = cases,
                                selectedColumns = selectedColumns
                            )

                    showExport = false

                    exportLauncher.launch(
                        "eJagriti_Cases_${getTodayDate()}.xlsx"
                    )
                } catch (exception: Exception) {

                    pendingExportBytes = null

                    Toast.makeText(
                        context,
                        "Export failed: ${exception.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    deleteCase?.let { selectedCase ->

        AlertDialog(
            onDismissRequest = {
                deleteCase = null
            },

            title = {
                Text("Delete Case?")
            },

            text = {
                Text(
                    "Delete Litigation ID: ${selectedCase.litigationId}?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        viewModel.deleteCase(selectedCase.id)
                        deleteCase = null
                    }
                ) {
                    Text(
                        "Delete",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deleteCase = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class ExportColumn(
    val key: String,
    val label: String
)

private val exportColumns = listOf(
    ExportColumn("litigationId", "Litigation ID"),
    ExportColumn("newCaseNumber", "New Case Number"),
    ExportColumn("oldCaseNumber", "Old Case Number"),
    ExportColumn("partyName", "Complainant / Party"),
    ExportColumn("oppositeParty", "Opposite Party"),
    ExportColumn("courtCommission", "Commission"),
    ExportColumn("caseType", "Case Type"),
    ExportColumn("state", "State"),
    ExportColumn("district", "District"),
    ExportColumn("nextHearingDate", "Next Hearing Date"),
    ExportColumn("caseStatus", "Case Status")
)

@Composable
fun ExportColumnDialog(
    onDismiss: () -> Unit,
    onExport: (List<String>) -> Unit
) {

    var selectedKeys by remember {
        mutableStateOf(exportColumns.map { it.key }.toSet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                "Export Cases to Excel",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column {

                Text(
                    "Select the columns you want in the Excel file.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = selectedKeys.size == exportColumns.size,
                        onCheckedChange = { checked ->

                            selectedKeys =
                                if (checked) {
                                    exportColumns.map { it.key }.toSet()
                                } else {
                                    emptySet()
                                }
                        }
                    )

                    Text(
                        "Select all",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider()

                LazyColumn(
                    modifier = Modifier.height(320.dp)
                ) {

                    items(exportColumns) { column ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    selectedKeys.contains(column.key),

                                onCheckedChange = { checked ->

                                    selectedKeys =
                                        if (checked) {
                                            selectedKeys + column.key
                                        } else {
                                            selectedKeys - column.key
                                        }
                                }
                            )

                            Text(column.label)
                        }
                    }
                }
            }
        },

        confirmButton = {

            Button(
                enabled = selectedKeys.isNotEmpty(),
                onClick = {
                    onExport(
                        exportColumns
                            .filter { selectedKeys.contains(it.key) }
                            .map { it.key }
                    )
                }
            ) {
                Text("Create Excel")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CaseCard(
    caseItem: CaseEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = caseItem.partyName.ifBlank {
                            "Unnamed Case"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(
                        text = caseItem.caseStatus,
                        color = Blue,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CaseDetail("Litigation ID", caseItem.litigationId)
            CaseDetail("New Case No.", caseItem.newCaseNumber)
            CaseDetail("Old Case No.", caseItem.oldCaseNumber)
            CaseDetail("Next Hearing", caseItem.nextHearingDate)
        }
    }
}

@Composable
fun CaseDetailsScreen(
    caseItem: CaseEntity,
    viewModel: CaseViewModel,
    onBack: () -> Unit
) {

    var showEdit by remember {
        mutableStateOf(false)
    }

    var showDelete by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = LightBackground
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(paddingValues)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                        text = "Case Details",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                }
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Navy
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = caseItem.partyName.ifBlank {
                                "Unnamed Case"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = caseItem.caseStatus,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            item {

                DetailsSection(
                    title = "Case Identification",
                    details = listOf(
                        "Litigation ID" to caseItem.litigationId,
                        "New Case Number" to caseItem.newCaseNumber,
                        "Old Case Number" to caseItem.oldCaseNumber
                    )
                )
            }

            item {

                DetailsSection(
                    title = "Parties",
                    details = listOf(
                        "Complainant / Party" to caseItem.partyName,
                        "Opposite Party" to caseItem.oppositeParty
                    )
                )
            }

            item {

                DetailsSection(
                    title = "Case Information",
                    details = listOf(
                        "Commission" to caseItem.courtCommission,
                        "Case Type" to caseItem.caseType,
                        "State" to caseItem.state,
                        "District" to caseItem.district,
                        "Case Status" to caseItem.caseStatus
                    )
                )
            }

            item {

                DetailsSection(
                    title = "Hearing",
                    details = listOf(
                        "Next Hearing Date" to caseItem.nextHearingDate
                    )
                )
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        modifier = Modifier.weight(1f),

                        onClick = {
                            showEdit = true
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        )
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text("Edit Case")
                    }

                    Button(
                        modifier = Modifier.weight(1f),

                        onClick = {
                            showDelete = true
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {

                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text("Delete")
                    }
                }
            }
        }
    }

    if (showEdit) {

        CaseFormDialog(
            title = "Edit Case",
            initialCase = caseItem,

            onDismiss = {
                showEdit = false
            },

            onSave = { form ->

                viewModel.updateCase(
                    caseItem.copy(
                        litigationId = form.litigationId,
                        newCaseNumber = form.newCaseNumber,
                        oldCaseNumber = form.oldCaseNumber,
                        partyName = form.partyName,
                        oppositeParty = form.oppositeParty,
                        courtCommission = form.courtCommission,
                        caseType = form.caseType,
                        state = form.state,
                        district = form.district,
                        nextHearingDate = form.nextHearingDate,
                        caseStatus = form.caseStatus
                    )
                )

                showEdit = false
            }
        )
    }

    if (showDelete) {

        AlertDialog(

            onDismissRequest = {
                showDelete = false
            },

            title = {
                Text("Delete Case?")
            },

            text = {
                Text(
                    "Are you sure you want to permanently delete this case?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.deleteCase(caseItem.id)

                        showDelete = false

                        onBack()
                    }
                ) {

                    Text(
                        "Delete",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDelete = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailsSection(
    title: String,
    details: List<Pair<String, String>>
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            details.forEach { detail ->

                if (detail.second.isNotBlank()) {

                    Text(
                        text = detail.first,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = detail.second,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CaseDetail(
    label: String,
    value: String
) {

    if (value.isNotBlank()) {

        Row(
            modifier = Modifier.padding(vertical = 2.dp)
        ) {

            Text(
                text = "$label: ",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HearingsScreen(
    cases: List<CaseEntity>,
    onCaseClick: (CaseEntity) -> Unit,
    modifier: Modifier = Modifier
) {

    val today = getTodayDate()

    var displayedYear by remember {
        mutableIntStateOf(getCurrentYear())
    }

    var displayedMonth by remember {
        mutableIntStateOf(getCurrentMonth())
    }

    var selectedDate by remember {
        mutableStateOf(today)
    }

    val monthPrefix =
        "%04d-%02d".format(
            displayedYear,
            displayedMonth + 1
        )

    val monthCases = cases
        .filter {
            it.nextHearingDate.startsWith(monthPrefix)
        }
        .sortedBy {
            it.nextHearingDate
        }

    val selectedCases = cases
        .filter {
            it.nextHearingDate == selectedDate
        }
        .sortedBy {
            it.partyName
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text = "Hearings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Text(
                text = "Monthly hearing schedule",
                color = Color.Gray
            )
        }

        item {

            MonthHeader(
                year = displayedYear,
                month = displayedMonth,

                onPrevious = {

                    if (displayedMonth == 0) {
                        displayedMonth = 11
                        displayedYear--
                    } else {
                        displayedMonth--
                    }

                    selectedDate =
                        "%04d-%02d-%02d".format(
                            displayedYear,
                            displayedMonth + 1,
                            1
                        )
                },

                onNext = {

                    if (displayedMonth == 11) {
                        displayedMonth = 0
                        displayedYear++
                    } else {
                        displayedMonth++
                    }

                    selectedDate =
                        "%04d-%02d-%02d".format(
                            displayedYear,
                            displayedMonth + 1,
                            1
                        )
                }
            )
        }

        item {

            CalendarView(
                year = displayedYear,
                month = displayedMonth,
                hearingDates = monthCases
                    .map { it.nextHearingDate }
                    .toSet(),
                selectedDate = selectedDate,

                onDateSelected = {
                    selectedDate = it
                }
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Selected Date",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = formatDisplayDate(selectedDate),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(
                        text = "${selectedCases.size} hearing(s)",
                        fontSize = 13.sp,
                        color = Blue
                    )
                }
            }
        }

        if (selectedCases.isEmpty()) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "No hearings on this date.",
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Select another date from the calendar.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        } else {

            items(
                items = selectedCases,
                key = { it.id }
            ) { caseItem ->

                HearingCaseCard(
                    caseItem = caseItem,
                    onClick = {
                        onCaseClick(caseItem)
                    }
                )
            }
        }

        item {

            Text(
                text = "All Hearings in ${getMonthName(displayedMonth)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        if (monthCases.isEmpty()) {

            item {

                Text(
                    text = "No hearings scheduled this month.",
                    color = Color.Gray
                )
            }

        } else {

            items(
                items = monthCases,
                key = { "month-${it.id}" }
            ) { caseItem ->

                HearingCaseCard(
                    caseItem = caseItem,
                    onClick = {
                        onCaseClick(caseItem)
                    }
                )
            }
        }
    }
}

@Composable
fun MonthHeader(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            IconButton(
                onClick = onPrevious
            ) {

                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Previous month"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = getMonthName(month),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Text(
                    text = year.toString(),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onNext
            ) {

                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next month"
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    year: Int,
    month: Int,
    hearingDates: Set<String>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {

    val calendar = Calendar.getInstance()

    calendar.set(
        year,
        month,
        1
    )

    val firstDayOfWeek =
        calendar.get(Calendar.DAY_OF_WEEK)

    val daysInMonth =
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val leadingEmptyDays =
        firstDayOfWeek - Calendar.SUNDAY

    val totalCells =
        leadingEmptyDays + daysInMonth

    val rows =
        (totalCells + 6) / 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                listOf(
                    "S",
                    "M",
                    "T",
                    "W",
                    "T",
                    "F",
                    "S"
                ).forEach { day ->

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            repeat(rows) { row ->

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    repeat(7) { column ->

                        val cellIndex =
                            row * 7 + column

                        val day =
                            cellIndex - leadingEmptyDays + 1

                        if (
                            day in 1..daysInMonth
                        ) {

                            val date =
                                "%04d-%02d-%02d".format(
                                    year,
                                    month + 1,
                                    day
                                )

                            val hasHearing =
                                hearingDates.contains(date)

                            val isSelected =
                                selectedDate == date

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .background(
                                        color =
                                            if (isSelected) {
                                                Blue
                                            } else if (hasHearing) {
                                                Orange.copy(alpha = 0.15f)
                                            } else {
                                                Color.Transparent
                                            },
                                        shape =
                                            RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onDateSelected(date)
                                    }
                                    .padding(vertical = 10.dp),

                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        fontWeight =
                                            if (
                                                hasHearing ||
                                                isSelected
                                            ) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        color =
                                            if (isSelected) {
                                                Color.White
                                            } else {
                                                Navy
                                            }
                                    )

                                    if (hasHearing) {

                                        Spacer(
                                            modifier =
                                                Modifier.height(2.dp)
                                        )

                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(5.dp)
                                                    .background(
                                                        if (isSelected) {
                                                            Color.White
                                                        } else {
                                                            Orange
                                                        },
                                                        RoundedCornerShape(
                                                            50
                                                        )
                                                    )
                                        )
                                    }
                                }
                            }

                        } else {

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .height(45.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HearingCaseCard(
    caseItem: CaseEntity,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = caseItem.partyName.ifBlank {
                            "Unnamed Case"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(
                        text = caseItem.nextHearingDate,
                        fontSize = 13.sp,
                        color = Blue
                    )
                }

                Text(
                    text = caseItem.caseStatus,
                    fontSize = 12.sp,
                    color = Green,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            CaseDetail(
                "Litigation ID",
                caseItem.litigationId
            )

            CaseDetail(
                "Old Case No.",
                caseItem.oldCaseNumber
            )

            CaseDetail(
                "New Case No.",
                caseItem.newCaseNumber
            )

            CaseDetail(
                "Commission",
                caseItem.courtCommission
            )
        }
    }
}

data class CaseFormData(
    val litigationId: String,
    val newCaseNumber: String,
    val oldCaseNumber: String,
    val partyName: String,
    val oppositeParty: String,
    val courtCommission: String,
    val caseType: String,
    val state: String,
    val district: String,
    val nextHearingDate: String,
    val caseStatus: String
)

@Composable
fun CaseFormDialog(
    title: String,
    initialCase: CaseEntity?,
    onDismiss: () -> Unit,
    onSave: (CaseFormData) -> Unit
) {

    var litigationId by remember {
        mutableStateOf(initialCase?.litigationId ?: "")
    }

    var newCaseNumber by remember {
        mutableStateOf(initialCase?.newCaseNumber ?: "")
    }

    var oldCaseNumber by remember {
        mutableStateOf(initialCase?.oldCaseNumber ?: "")
    }

    var partyName by remember {
        mutableStateOf(initialCase?.partyName ?: "")
    }

    var oppositeParty by remember {
        mutableStateOf(initialCase?.oppositeParty ?: "")
    }

    var courtCommission by remember {
        mutableStateOf(
            initialCase?.courtCommission
                ?: "District Consumer Disputes Redressal Commission"
        )
    }

    var caseType by remember {
        mutableStateOf(
            initialCase?.caseType
                ?: "Consumer Complaint"
        )
    }

    var state by remember {
        mutableStateOf(
            initialCase?.state
                ?: "Maharashtra"
        )
    }

    var district by remember {
        mutableStateOf(initialCase?.district ?: "")
    }

    var hearingDate by remember {
        mutableStateOf(initialCase?.nextHearingDate ?: "")
    }

    var caseStatus by remember {
        mutableStateOf(
            initialCase?.caseStatus
                ?: "Pending"
        )
    }

    var error by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                item {

                    OutlinedTextField(
                        value = litigationId,
                        onValueChange = {
                            litigationId = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Litigation ID *")
                        },
                        singleLine = true
                    )
                }

                item {

                    OutlinedTextField(
                        value = newCaseNumber,
                        onValueChange = {
                            newCaseNumber = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("New Case Number")
                        },
                        singleLine = true
                    )
                }

                item {

                    OutlinedTextField(
                        value = oldCaseNumber,
                        onValueChange = {
                            oldCaseNumber = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Old Case Number")
                        },
                        singleLine = true
                    )
                }

                item {

                    OutlinedTextField(
                        value = partyName,
                        onValueChange = {
                            partyName = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Complainant / Party *")
                        }
                    )
                }

                item {

                    OutlinedTextField(
                        value = oppositeParty,
                        onValueChange = {
                            oppositeParty = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Opposite Party")
                        }
                    )
                }

                item {

                    SimpleDropdown(
                        label = "Commission",
                        selectedValue = courtCommission,
                        options = listOf(
                            "District Consumer Disputes Redressal Commission",
                            "State Consumer Disputes Redressal Commission",
                            "National Consumer Disputes Redressal Commission"
                        ),
                        onValueSelected = {
                            courtCommission = it
                        }
                    )
                }

                item {

                    SimpleDropdown(
                        label = "Case Type",
                        selectedValue = caseType,
                        options = listOf(
                            "Consumer Complaint",
                            "First Appeal",
                            "Revision Petition",
                            "Execution Application",
                            "Review Application",
                            "Miscellaneous Application"
                        ),
                        onValueSelected = {
                            caseType = it
                        }
                    )
                }

                item {

                    SimpleDropdown(
                        label = "State",
                        selectedValue = state,
                        options = listOf(
                            "Maharashtra",
                            "Delhi",
                            "Gujarat",
                            "Karnataka",
                            "Tamil Nadu",
                            "Uttar Pradesh",
                            "Rajasthan",
                            "Madhya Pradesh",
                            "Goa",
                            "Kerala",
                            "West Bengal",
                            "Telangana",
                            "Andhra Pradesh",
                            "Haryana",
                            "Punjab",
                            "Bihar",
                            "Odisha",
                            "Other"
                        ),
                        onValueSelected = {
                            state = it
                            if (district !in districtsForState(it, district)) {
                                district = ""
                            }
                        }
                    )
                }

                item {

                    SimpleDropdown(
                        label = "District",
                        selectedValue = district.ifBlank { "Select district" },
                        options = districtsForState(state, district),
                        onValueSelected = {
                            district = if (it == "Select district") "" else it
                        }
                    )
                }

                item {

                    DatePickerField(
                        value = hearingDate,
                        onDateSelected = {
                            hearingDate = it
                        }
                    )
                }

                item {

                    SimpleDropdown(
                        label = "Final Status",
                        selectedValue = caseStatus,
                        options = listOf(
                            "Pending",
                            "Disposed",
                            "Stayed",
                            "Withdrawn",
                            "Settled",
                            "Dismissed",
                            "Allowed",
                            "Partly Allowed",
                            "Transferred"
                        ),
                        onValueSelected = {
                            caseStatus = it
                        }
                    )
                }

                if (error.isNotBlank()) {

                    item {

                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    if (
                        litigationId.isBlank() ||
                        partyName.isBlank()
                    ) {

                        error =
                            "Litigation ID and Party Name are required."

                    } else {

                        onSave(
                            CaseFormData(
                                litigationId = litigationId,
                                newCaseNumber = newCaseNumber,
                                oldCaseNumber = oldCaseNumber,
                                partyName = partyName,
                                oppositeParty = oppositeParty,
                                courtCommission = courtCommission,
                                caseType = caseType,
                                state = state,
                                district = district,
                                nextHearingDate = hearingDate,
                                caseStatus = caseStatus
                            )
                        )
                    }
                }
            ) {

                Text(
                    if (initialCase == null) "Save" else "Update"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SimpleDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                singleLine = true
            )
        }

        DropdownMenu(
            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { option ->

                DropdownMenuItem(

                    text = {
                        Text(option)
                    },

                    onClick = {

                        onValueSelected(option)

                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit
) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column {

        Text(
            text = "Next Hearing Date",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    value.split("-").takeIf { it.size == 3 }?.let { parts ->
                        parts[0].toIntOrNull()?.let { calendar.set(Calendar.YEAR, it) }
                        parts[1].toIntOrNull()?.let { calendar.set(Calendar.MONTH, it - 1) }
                        parts[2].toIntOrNull()?.let { calendar.set(Calendar.DAY_OF_MONTH, it) }
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            onDateSelected("%04d-%02d-%02d".format(year, month + 1, day))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Choose next hearing date"
                    )
                }
            )
        }
    }
}

private fun districtsForState(state: String, currentDistrict: String): List<String> {
    val districts = when (state) {
        "Maharashtra" -> listOf("Ahmednagar", "Akola", "Amravati", "Chhatrapati Sambhajinagar", "Mumbai City", "Mumbai Suburban", "Nagpur", "Nashik", "Pune", "Solapur", "Thane")
        "Delhi" -> listOf("Central Delhi", "East Delhi", "New Delhi", "North Delhi", "North East Delhi", "North West Delhi", "Shahdara", "South Delhi", "South East Delhi", "South West Delhi", "West Delhi")
        "Gujarat" -> listOf("Ahmedabad", "Anand", "Bhavnagar", "Jamnagar", "Rajkot", "Surat", "Vadodara")
        "Karnataka" -> listOf("Bengaluru Urban", "Belagavi", "Dakshina Kannada", "Mysuru", "Shivamogga", "Udupi")
        "Tamil Nadu" -> listOf("Chennai", "Coimbatore", "Madurai", "Salem", "Tiruchirappalli", "Tirunelveli")
        "Uttar Pradesh" -> listOf("Agra", "Allahabad", "Bareilly", "Ghaziabad", "Gorakhpur", "Kanpur Nagar", "Lucknow", "Meerut", "Varanasi")
        "Rajasthan" -> listOf("Ajmer", "Bikaner", "Jaipur", "Jodhpur", "Kota", "Udaipur")
        "Madhya Pradesh" -> listOf("Bhopal", "Gwalior", "Indore", "Jabalpur", "Ujjain")
        "Goa" -> listOf("North Goa", "South Goa")
        "Kerala" -> listOf("Ernakulam", "Kozhikode", "Thiruvananthapuram", "Thrissur")
        "West Bengal" -> listOf("Howrah", "Kolkata", "North 24 Parganas", "Siliguri")
        "Telangana" -> listOf("Hyderabad", "Medchal-Malkajgiri", "Rangareddy", "Warangal")
        "Andhra Pradesh" -> listOf("Guntur", "Krishna", "Sri Potti Sriramulu Nellore", "Visakhapatnam")
        "Haryana" -> listOf("Faridabad", "Gurugram", "Hisar", "Panchkula", "Sonipat")
        "Punjab" -> listOf("Amritsar", "Bathinda", "Jalandhar", "Ludhiana", "Patiala")
        "Bihar" -> listOf("Bhagalpur", "Gaya", "Muzaffarpur", "Patna")
        "Odisha" -> listOf("Bhubaneswar", "Cuttack", "Puri", "Sambalpur")
        else -> listOf("Other district")
    }

    return buildList {
        add("Select district")
        if (currentDistrict.isNotBlank() && currentDistrict !in districts) add(currentDistrict)
        addAll(districts)
    }
}

fun getTodayDate(): String {

    val calendar = Calendar.getInstance()

    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun getTomorrowDate(): String {

    val calendar = Calendar.getInstance()

    calendar.add(
        Calendar.DAY_OF_MONTH,
        1
    )

    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun getCurrentMonthPrefix(): String {

    val calendar = Calendar.getInstance()

    return "%04d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1
    )
}

fun getCurrentYear(): Int {

    return Calendar.getInstance()
        .get(Calendar.YEAR)
}

fun getCurrentMonth(): Int {

    return Calendar.getInstance()
        .get(Calendar.MONTH)
}

fun getMonthName(month: Int): String {

    return listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )[month]
}

fun formatDisplayDate(date: String): String {

    if (date.length != 10) {
        return date
    }

    val parts = date.split("-")

    if (parts.size != 3) {
        return date
    }

    val month = parts[1].toIntOrNull() ?: return date
    val day = parts[2].toIntOrNull() ?: return date

    if (month !in 1..12) {
        return date
    }

    return "${day} ${getMonthName(month - 1)} ${parts[0]}"
}
