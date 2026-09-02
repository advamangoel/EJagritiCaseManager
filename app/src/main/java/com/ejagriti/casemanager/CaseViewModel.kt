package com.ejagriti.casemanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejagriti.casemanager.data.AppDatabase
import com.ejagriti.casemanager.data.CaseEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaseViewModel(application: Application) : AndroidViewModel(application) {

    private val caseDao =
        AppDatabase.getDatabase(application).caseDao()

    private val _cases =
        MutableStateFlow<List<CaseEntity>>(emptyList())

    val cases: StateFlow<List<CaseEntity>> =
        _cases.asStateFlow()

    private val _searchQuery =
        MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    private var casesJob: Job? = null

    init {
        observeAllCases()
    }

    private fun observeAllCases() {

        casesJob?.cancel()

        casesJob = viewModelScope.launch {

            caseDao.getAllCases().collect { caseList ->
                _cases.value = caseList
            }
        }
    }

    fun searchCases(query: String) {

        _searchQuery.value = query

        casesJob?.cancel()

        casesJob = viewModelScope.launch {

            if (query.isBlank()) {

                caseDao.getAllCases().collect { caseList ->
                    _cases.value = caseList
                }

            } else {

                caseDao.searchCases(query.trim())
                    .collect { caseList ->
                        _cases.value = caseList
                    }
            }
        }
    }

    fun addCase(
        litigationId: String,
        newCaseNumber: String,
        oldCaseNumber: String,
        partyName: String,
        oppositeParty: String,
        courtCommission: String,
        caseType: String,
        state: String,
        district: String,
        nextHearingDate: String,
        caseStatus: String
    ) {

        viewModelScope.launch {

            val newCase = CaseEntity(
                litigationId = litigationId.trim(),
                newCaseNumber = newCaseNumber.trim(),
                oldCaseNumber = oldCaseNumber.trim(),
                partyName = partyName.trim(),
                oppositeParty = oppositeParty.trim(),
                courtCommission = courtCommission,
                caseType = caseType,
                state = state,
                district = district,
                nextHearingDate = nextHearingDate,
                caseStatus = caseStatus
            )

            caseDao.insertCase(newCase)
        }
    }

    fun updateCase(caseEntity: CaseEntity) {

        viewModelScope.launch {

            caseDao.updateCase(
                caseEntity.copy(
                    litigationId = caseEntity.litigationId.trim(),
                    newCaseNumber = caseEntity.newCaseNumber.trim(),
                    oldCaseNumber = caseEntity.oldCaseNumber.trim(),
                    partyName = caseEntity.partyName.trim(),
                    oppositeParty = caseEntity.oppositeParty.trim()
                )
            )
        }
    }

    fun deleteCase(id: Long) {

        viewModelScope.launch {
            caseDao.deleteCase(id)
        }
    }
}