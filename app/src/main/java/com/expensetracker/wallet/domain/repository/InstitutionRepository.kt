package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.Institution

/**
 * plan.md §11 lists a full institutions catalog (logos, etc.) as a later enhancement; Phase 3
 * only needs free-text institution capture per its "Out of scope: Institution catalog/logos"
 * note, so this interface is intentionally minimal — [findOrCreateByName] is what backs the
 * account form's plain-text institution field. Implemented in Phase 3.
 */
interface InstitutionRepository {
    suspend fun getById(id: String): Institution?
    suspend fun findOrCreateByName(name: String): Institution
}
