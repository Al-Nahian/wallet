package com.example.wallet.feature.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.BudgetRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.usecase.importexport.ExportCsv
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ExportEntityType(val label: String, val suggestedFileName: String) {
    TRANSACTIONS("Transactions", "wallet_transactions.csv"),
    ACCOUNTS("Accounts", "wallet_accounts.csv"),
    BUDGETS("Budgets", "wallet_budgets.csv"),
    CATEGORIES("Categories", "wallet_categories.csv"),
    LABELS("Labels", "wallet_labels.csv"),
}

data class ImportExportUiState(
    val isExporting: Boolean = false,
    val statusMessage: String? = null,
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val labelRepository: LabelRepository,
    private val institutionRepository: InstitutionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState

    fun exportTo(entityType: ExportEntityType, destination: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, statusMessage = null)
            val result = runCatching { buildCsv(entityType) }
            result.onSuccess { csv ->
                writeToUri(destination, csv)
                _uiState.value = ImportExportUiState(statusMessage = "${entityType.label} exported.")
            }.onFailure {
                _uiState.value = ImportExportUiState(statusMessage = "Export failed: ${it.message}")
            }
        }
    }

    private suspend fun buildCsv(entityType: ExportEntityType): String = when (entityType) {
        ExportEntityType.TRANSACTIONS -> {
            val transactions = transactionRepository.observeTransactions().first()
            val accounts = accountRepository.observeAllAccounts().first()
            val categories = categoryRepository.observeCategories().first()
            ExportCsv.transactionsToCsv(
                transactions = transactions,
                accountNameById = accounts.associate { it.id to it.name },
                categoryNameById = categories.associate { it.id to it.name },
            )
        }
        ExportEntityType.ACCOUNTS -> {
            val accounts = accountRepository.observeAllAccounts().first()
            val institutionIds = accounts.mapNotNull { it.institutionId }.distinct()
            val institutionById = institutionIds.mapNotNull { institutionRepository.getById(it) }
                .associateBy { it.id }
            ExportCsv.accountsToCsv(accounts, institutionById)
        }
        ExportEntityType.BUDGETS -> ExportCsv.budgetsToCsv(budgetRepository.observeBudgets().first())
        ExportEntityType.CATEGORIES -> {
            val groups = categoryRepository.observeGroups().first()
            val categories = categoryRepository.observeCategories().first()
            ExportCsv.categoriesToCsv(groups, categories)
        }
        ExportEntityType.LABELS -> ExportCsv.labelsToCsv(labelRepository.observeLabels().first())
    }

    private suspend fun writeToUri(uri: Uri, content: String) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
        }
    }

    fun consumeStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
