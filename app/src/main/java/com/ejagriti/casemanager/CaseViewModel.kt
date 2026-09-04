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

data class ImportSaveResult(
    val added: Int,
    val updated: Int,
    val skipped: Int,
    val conflicts: Int
)

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
        caseStatus: String,
        onResult: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val cleanLitigationId = litigationId.trim()
            val cleanNewCaseNumber = newCaseNumber.trim()
            val cleanOldCaseNumber = oldCaseNumber.trim()

            val duplicate = caseDao.findPotentialDuplicates(
                litigationId = cleanLitigationId,
                newCaseNumber = cleanNewCaseNumber,
                oldCaseNumber = cleanOldCaseNumber
            ).firstOrNull()

            if (duplicate != null) {
                onResult?.invoke(
                    "Duplicate detected. Existing Litigation ID: " +
                        duplicate.litigationId
                )
                return@launch
            }

            val newCase = CaseEntity(
                litigationId = cleanLitigationId,
                newCaseNumber = cleanNewCaseNumber,
                oldCaseNumber = cleanOldCaseNumber,
                partyName = partyName.trim(),
                oppositeParty = oppositeParty.trim(),
                courtCommission = courtCommission.trim(),
                caseType = caseType.trim(),
                state = state.trim(),
                district = district.trim(),
                nextHearingDate = nextHearingDate.trim(),
                caseStatus = caseStatus.trim()
            )

            caseDao.insertCase(newCase)

            onResult?.invoke("Case saved successfully.")
        }
    }

    fun updateCase(
        caseEntity: CaseEntity,
        onResult: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val clean = caseEntity.copy(
                litigationId = caseEntity.litigationId.trim(),
                newCaseNumber = caseEntity.newCaseNumber.trim(),
                oldCaseNumber = caseEntity.oldCaseNumber.trim(),
                partyName = caseEntity.partyName.trim(),
                oppositeParty = caseEntity.oppositeParty.trim(),
                courtCommission = caseEntity.courtCommission.trim(),
                caseType = caseEntity.caseType.trim(),
                state = caseEntity.state.trim(),
                district = caseEntity.district.trim(),
                nextHearingDate = caseEntity.nextHearingDate.trim(),
                caseStatus = caseEntity.caseStatus.trim()
            )

            val duplicates = caseDao.findPotentialDuplicates(
                litigationId = clean.litigationId,
                newCaseNumber = clean.newCaseNumber,
                oldCaseNumber = clean.oldCaseNumber
            ).filter { it.id != clean.id }

            if (duplicates.isNotEmpty()) {
                onResult?.invoke(
                    "Update blocked: another case already uses " +
                        "the same Litigation ID or case number."
                )
                return@launch
            }

            caseDao.updateCase(clean)
            onResult?.invoke("Case updated successfully.")
        }
    }

    fun deleteCase(id: Long) {
        viewModelScope.launch {
            caseDao.deleteCase(id)
        }
    }

    /**
     * Safe bulk import:
     * - New record -> ADD
     * - Matching Litigation ID / New Case No. / Old Case No. -> UPDATE existing
     * - If one imported record matches different existing records through
     *   different identifiers, it is treated as a conflict and skipped.
     *
     * Existing Litigation ID is preserved when OCR/Excel does not contain it.
     */
    fun importCases(
        importedCases: List<CaseEntity>,
        onFinished: (ImportSaveResult) -> Unit
    ) {
        viewModelScope.launch {
            var added = 0
            var updated = 0
            var skipped = 0
            var conflicts = 0

            for (raw in importedCases) {
                val incoming = raw.copy(
                    litigationId = raw.litigationId.trim(),
                    newCaseNumber = raw.newCaseNumber.trim(),
                    oldCaseNumber = raw.oldCaseNumber.trim(),
                    partyName = raw.partyName.trim(),
                    oppositeParty = raw.oppositeParty.trim(),
                    courtCommission = raw.courtCommission.trim(),
                    caseType = raw.caseType.trim(),
                    state = raw.state.trim(),
                    district = raw.district.trim(),
                    nextHearingDate = raw.nextHearingDate.trim(),
                    caseStatus = raw.caseStatus.trim()
                )

                if (
                    incoming.litigationId.isBlank() &&
                    incoming.newCaseNumber.isBlank() &&
                    incoming.oldCaseNumber.isBlank()
                ) {
                    skipped++
                    continue
                }

                val matches = caseDao.findPotentialDuplicates(
                    litigationId = incoming.litigationId,
                    newCaseNumber = incoming.newCaseNumber,
                    oldCaseNumber = incoming.oldCaseNumber
                ).distinctBy { it.id }

                if (matches.size > 1) {
                    conflicts++
                    continue
                }

                val existing = matches.firstOrNull()

                if (existing == null) {
                    caseDao.insertCase(incoming.copy(id = 0))
                    added++
                } else {
                    val merged = existing.copy(
                        // Never erase a known Litigation ID because OCR missed it.
                        litigationId = incoming.litigationId.ifBlank {
                            existing.litigationId
                        },
                        newCaseNumber = incoming.newCaseNumber.ifBlank {
                            existing.newCaseNumber
                        },
                        oldCaseNumber = incoming.oldCaseNumber.ifBlank {
                            existing.oldCaseNumber
                        },
                        partyName = incoming.partyName.ifBlank {
                            existing.partyName
                        },
                        oppositeParty = incoming.oppositeParty.ifBlank {
                            existing.oppositeParty
                        },
                        courtCommission = incoming.courtCommission.ifBlank {
                            existing.courtCommission
                        },
                        caseType = incoming.caseType.ifBlank {
                            existing.caseType
                        },
                        state = incoming.state.ifBlank {
                            existing.state
                        },
                        district = incoming.district.ifBlank {
                            existing.district
                        },
                        nextHearingDate = incoming.nextHearingDate.ifBlank {
                            existing.nextHearingDate
                        },
                        caseStatus = incoming.caseStatus.ifBlank {
                            existing.caseStatus
                        }
                    )

                    caseDao.updateCase(merged)
                    updated++
                }
            }

            onFinished(
                ImportSaveResult(
                    added = added,
                    updated = updated,
                    skipped = skipped,
                    conflicts = conflicts
                )
            )
        }
    }
}
