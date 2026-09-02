package com.ejagriti.casemanager

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.util.Calendar

private val Navy = Color(0xFF14213D)
private val Blue = Color(0xFF1D4ED8)
private val Orange = Color(0xFFD97706)
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

    var selectedTab by remember { mutableStateOf(0) }
    var showAddCase by remember { mutableStateOf(false) }

    val cases by viewModel.cases.collectAsState()

    Scaffold(
        containerColor = LightBackground,

        floatingActionButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = { showAddCase = true },
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
                modifier = Modifier.padding(paddingValues)
            )

            1 -> CasesScreen(
                cases = cases,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )

            2 -> HearingsScreen(
                cases = cases,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (showAddCase) {

        AddCaseDialog(

            onDismiss = {
                showAddCase = false
            },

            onSave = { caseData ->

                viewModel.addCase(
                    litigationId = caseData.litigationId,
                    newCaseNumber = caseData.newCaseNumber,
                    oldCaseNumber = caseData.oldCaseNumber,
                    partyName = caseData.partyName,
                    oppositeParty = caseData.oppositeParty,
                    courtCommission = caseData.courtCommission,
                    caseType = caseData.caseType,
                    state = caseData.state,
                    district = caseData.district,
                    nextHearingDate = caseData.nextHearingDate,
                    caseStatus = caseData.caseStatus
                )

                showAddCase = false
            }
        )
    }
}

@Composable
fun DashboardScreen(
    cases: List<CaseEntity>,
    modifier: Modifier = Modifier
) {

    val todayCount = cases.count {
        it.nextHearingDate == getTodayDate()
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
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todayCount.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = cases.size.toString(),
                    label = "Total Cases",
                    color = Blue
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    value = cases.count {
                        it.caseStatus == "Pending"
                    }.toString(),
                    label = "Pending",
                    color = Orange
                )
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
    modifier: Modifier = Modifier
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var deleteCase by remember {
        mutableStateOf<CaseEntity?>(null)
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
                    Text(
                        "Search Litigation ID / New / Old Case No."
                    )
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

                    onDelete = {
                        deleteCase = caseItem
                    }
                )
            }
        }
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

@Composable
fun CaseCard(
    caseItem: CaseEntity,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
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

            CaseDetail(
                "Litigation ID",
                caseItem.litigationId
            )

            CaseDetail(
                "New Case No.",
                caseItem.newCaseNumber
            )

            CaseDetail(
                "Old Case No.",
                caseItem.oldCaseNumber
            )

            CaseDetail(
                "Next Hearing",
                caseItem.nextHearingDate
            )
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
    modifier: Modifier = Modifier
) {

    val hearingCases = cases
        .filter {
            it.nextHearingDate.isNotBlank()
        }
        .sortedBy {
            it.nextHearingDate
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
                text = "Upcoming hearing schedule",
                color = Color.Gray
            )
        }

        if (hearingCases.isEmpty()) {

            item {

                Text(
                    text = "No hearings scheduled yet",
                    modifier = Modifier.padding(top = 30.dp),
                    color = Color.Gray
                )
            }

        } else {

            items(hearingCases) { caseItem ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = caseItem.nextHearingDate,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = caseItem.partyName,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Litigation ID: ${caseItem.litigationId}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
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
fun AddCaseDialog(
    onDismiss: () -> Unit,
    onSave: (CaseFormData) -> Unit
) {

    var litigationId by remember {
        mutableStateOf("")
    }

    var newCaseNumber by remember {
        mutableStateOf("")
    }

    var oldCaseNumber by remember {
        mutableStateOf("")
    }

    var partyName by remember {
        mutableStateOf("")
    }

    var oppositeParty by remember {
        mutableStateOf("")
    }

    var courtCommission by remember {
        mutableStateOf("District Consumer Commission")
    }

    var caseType by remember {
        mutableStateOf("Consumer Complaint")
    }

    var state by remember {
        mutableStateOf("Maharashtra")
    }

    var district by remember {
        mutableStateOf("")
    }

    var hearingDate by remember {
        mutableStateOf("")
    }

    var caseStatus by remember {
        mutableStateOf("Pending")
    }

    var error by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                "Add New Case",
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
                            "District Consumer Commission",
                            "State Consumer Commission",
                            "National Consumer Commission"
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
                            "Madhya Pradesh"
                        ),
                        onValueSelected = {
                            state = it
                        }
                    )
                }

                item {

                    OutlinedTextField(
                        value = district,
                        onValueChange = {
                            district = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("District")
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
                        label = "Case Status",
                        selectedValue = caseStatus,
                        options = listOf(
                            "Pending",
                            "Disposed",
                            "Stayed",
                            "Withdrawn"
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
                Text("Save")
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

        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                },
            readOnly = true,
            singleLine = true
        )

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

        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    DatePickerDialog(
                        context,

                        { _, year, month, day ->

                            val formattedDate =
                                "%04d-%02d-%02d".format(
                                    year,
                                    month + 1,
                                    day
                                )

                            onDateSelected(formattedDate)
                        },

                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)

                    ).show()
                },

            readOnly = true,

            trailingIcon = {

                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            }
        )
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