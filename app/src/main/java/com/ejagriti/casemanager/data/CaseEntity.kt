package com.ejagriti.casemanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Organisation's unique litigation reference
    val litigationId: String,

    // Current e-Jagriti case number
    val newCaseNumber: String,

    // Previous / old case number
    val oldCaseNumber: String,

    val partyName: String,
    val oppositeParty: String,

    val courtCommission: String,
    val caseType: String,

    val state: String,
    val district: String,

    // Stored as YYYY-MM-DD
    val nextHearingDate: String,

    val caseStatus: String,

    val createdAt: Long = System.currentTimeMillis()
)