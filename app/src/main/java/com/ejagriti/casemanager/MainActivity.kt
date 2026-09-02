package com.ejagriti.casemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF14213D)
private val Blue = Color(0xFF1D4ED8)
private val LightBackground = Color(0xFFF6F7FB)
private val CardBackground = Color.White
private val Green = Color(0xFF15803D)
private val Orange = Color(0xFFD97706)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                EJagritiCaseManagerApp()
            }
        }
    }
}

@Composable
fun EJagritiCaseManagerApp() {

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = "Cases"
                        )
                    },
                    label = { Text("Cases") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Hearings"
                        )
                    },
                    label = { Text("Hearings") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More"
                        )
                    },
                    label = { Text("More") }
                )
            }
        }
    ) { paddingValues ->

        when (selectedTab) {
            0 -> DashboardScreen(
                modifier = Modifier.padding(paddingValues)
            )

            1 -> CasesScreen(
                modifier = Modifier.padding(paddingValues)
            )

            2 -> HearingsScreen(
                modifier = Modifier.padding(paddingValues)
            )

            3 -> MoreScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {

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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Litigation & Hearing Management",
                    fontSize = 14.sp,
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
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "TODAY'S HEARINGS",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "0",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "No hearings scheduled today",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                DashboardStat(
                    modifier = Modifier.weight(1f),
                    value = "0",
                    label = "Active Cases",
                    color = Blue
                )

                DashboardStat(
                    modifier = Modifier.weight(1f),
                    value = "0",
                    label = "This Month",
                    color = Green
                )
            }
        }

        item {

            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Add,
                title = "Add New Case",
                subtitle = "Create a case with Litigation ID and case numbers"
            )
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Search,
                title = "Search Case",
                subtitle = "Search by Litigation ID, New or Old Case Number"
            )
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Description,
                title = "Import Cases",
                subtitle = "Bulk import case information from documents"
            )
        }

        item {

            Text(
                text = "Upcoming Hearings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "No upcoming hearings",
                        fontWeight = FontWeight.SemiBold,
                        color = Navy
                    )

                    Text(
                        text = "Your hearing schedule will appear here",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun DashboardStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Blue.copy(alpha = 0.10f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Blue
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CasesScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {

        Text(
            text = "Cases",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage all litigation matters in one place",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Case Identification",
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                CaseIdentifier(
                    title = "Litigation ID",
                    subtitle = "Organisation unique identifier"
                )

                CaseIdentifier(
                    title = "New Case Number",
                    subtitle = "Current e-Jagriti case number"
                )

                CaseIdentifier(
                    title = "Old Case Number",
                    subtitle = "Previous / legacy case reference"
                )
            }
        }
    }
}

@Composable
fun CaseIdentifier(
    title: String,
    subtitle: String
) {

    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = Navy
        )

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()
    }
}

@Composable
fun HearingsScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {

        Text(
            text = "Hearings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Daily and monthly hearing schedule",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(30.dp))

        DashboardStat(
            modifier = Modifier.fillMaxWidth(),
            value = "0",
            label = "Hearings scheduled",
            color = Orange
        )
    }
}

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {

        Text(
            text = "More",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(modifier = Modifier.height(24.dp))

        MoreItem("Import PDF Cases")
        MoreItem("Export to Excel")
        MoreItem("Case Categories")
        MoreItem("Court / Commission List")
        MoreItem("Application Settings")
    }
}

@Composable
fun MoreItem(
    title: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(18.dp),
            fontWeight = FontWeight.Medium,
            color = Navy
        )
    }
}