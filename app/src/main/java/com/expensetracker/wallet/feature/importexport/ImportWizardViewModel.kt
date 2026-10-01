package com.expensetracker.wallet.feature.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.core.common.Csv
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.model.Template
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.BudgetRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.LabelRepository
import com.expensetracker.wallet.domain.repository.TemplateRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.usecase.importexport.ExportEntityType
import com.expensetracker.wallet.domain.usecase.importexport.ImportAccountsUseCase
import com.expensetracker.wallet.domain.usecase.importexport.ImportBudgetsUseCase
import com.expensetracker.wallet.domain.usecase.importexport.ImportCategoriesUseCase
import com.expensetracker.wallet.domain.usecase.importexport.ImportColumn
import com.expensetracker.wallet.domain.usecase.importexport.ImportLabelsUseCase
import com.expensetracker.wallet.domain.usecase.importexport.ImportRow
import com.expensetracker.wallet.domain.usecase.importexport.ImportTemplatesUseCase
import com.expensetracker.wallet.domain.usecase.importexport.ImportTransactionsUseCase
import com.expensetracker.wallet.domain.usecase.importexport.MapCsvRowsUseCase
import com.expensetracker.wallet.domain.usecase.importexport.detectColumnMapping
import com.expensetracker.wallet.domain.usecase.importexport.detectExportFormat
import com.expensetracker.wallet.domain.usecase.importexport.parseAccountRows
import com.expensetracker.wallet.domain.usecase.importexport.parseBudgetRows
import com.expensetracker.wallet.domain.usecase.importexport.parseCategoryRows
import com.expensetracker.wallet.domain.usecase.importexport.parseLabelRows
import com.expensetracker.wallet.domain.usecase.importexport.parseTemplateRows
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class WizardStep { LOADING, MAPPING, PREVIEW, ERROR }

private val REQUIRED_COLUMNS = listOf(ImportColumn.DATE, ImportColumn.AMOUNT, ImportColumn.ACCOUNT)

data class ImportWizardUiState(
    val step: WizardStep = WizardStep.LOADING,
    val detectedFormat: ExportEntityType = ExportEntityType.TRANSACTIONS,
    val headers: List<String> = emptyList(),
    val mapping: Map<ImportColumn, Int?> = emptyMap(),
    val rows: List<ImportRow> = emptyList(),
    val errorMessage: String? = null,
    val isCommitting: Boolean = false,
    val committed: Boolean = false,
    val importedCount: Int = 0,
)

/**
 * Recognizes this app's own export formats (accounts/budgets/categories/labels, by exact header
 * match — [detectExportFormat]) and routes straight to a preview for them; anything else (this
 * app's own transaction export included, plus arbitrary third-party CSVs) falls back to the
 * existing column-mapping flow, unchanged (plans/12-import-export.md).
 */
@HiltViewModel
class ImportWizardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val labelRepository: LabelRepository,
    private val budgetRepository: BudgetRepository,
    private val templateRepository: TemplateRepository,
    private val mapCsvRowsUseCase: MapCsvRowsUseCase,
    private val importTransactionsUseCase: ImportTransactionsUseCase,
    private val importAccountsUseCase: ImportAccountsUseCase,
    private val importCategoriesUseCase: ImportCategoriesUseCase,
    private val importLabelsUseCase: ImportLabelsUseCase,
    private val importBudgetsUseCase: ImportBudgetsUseCase,
    private val importTemplatesUseCase: ImportTemplatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportWizardUiState())
    val uiState: StateFlow<ImportWizardUiState> = _uiState

    private var bodyRows: List<List<String>> = emptyList()
    private var existingAccounts: List<Account> = emptyList()
    private var existingCategories: List<Category> = emptyList()
    private var existingCategoryGroups: List<CategoryGroup> = emptyList()
    private var existingLabels: List<Label> = emptyList()
    private var existingTransactions: List<Transaction> = emptyList()
    private var existingBudgets: List<Budget> = emptyList()
    private var existingTemplates: List<Template> = emptyList()

    init {
        val encodedUri = savedStateHandle.get<String>(ImportExportRoutes.URI_ARG)
        val uri = encodedUri?.let { Uri.parse(Uri.decode(it)) }
        if (uri == null) {
            _uiState.value = ImportWizardUiState(step = WizardStep.ERROR, errorMessage = "No file selected.")
        } else {
            loadFile(uri)
        }
    }

    private fun loadFile(uri: Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            if (text.isNullOrBlank()) {
                _uiState.value = ImportWizardUiState(step = WizardStep.ERROR, errorMessage = "Couldn't read that file.")
                return@launch
            }

            val parsed = Csv.parse(text).filter { row -> row.any { it.isNotBlank() } }
            if (parsed.isEmpty()) {
                _uiState.value = ImportWizardUiState(step = WizardStep.ERROR, errorMessage = "The file is empty.")
                return@launch
            }

            bodyRows = parsed.drop(1)
            existingAccounts = accountRepository.observeAllAccounts().first()
            existingCategories = categoryRepository.observeCategories().first()
            existingCategoryGroups = categoryRepository.observeGroups().first()
            existingLabels = labelRepository.observeLabels().first()
            existingTransactions = transactionRepository.observeTransactions().first()
            existingBudgets = budgetRepository.observeBudgets().first()
            existingTemplates = templateRepository.observeTemplates().first()

            val headers = parsed.first()
            when (detectExportFormat(headers)) {
                ExportEntityType.ACCOUNTS -> {
                    val rows = parseAccountRows(bodyRows, existingAccounts).map { ImportRow.AccountRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.ACCOUNTS,
                        rows = rows,
                    )
                }
                ExportEntityType.CATEGORIES -> {
                    val rows = parseCategoryRows(bodyRows, existingCategoryGroups, existingCategories)
                        .map { ImportRow.CategoryRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.CATEGORIES,
                        rows = rows,
                    )
                }
                ExportEntityType.LABELS -> {
                    val rows = parseLabelRows(bodyRows, existingLabels).map { ImportRow.LabelRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.LABELS,
                        rows = rows,
                    )
                }
                ExportEntityType.BUDGETS -> {
                    val rows = parseBudgetRows(bodyRows, existingBudgets).map { ImportRow.BudgetRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.BUDGETS,
                        rows = rows,
                    )
                }
                ExportEntityType.TEMPLATES -> {
                    val rows = parseTemplateRows(bodyRows, existingAccounts, existingCategories, existingLabels, existingTemplates)
                        .map { ImportRow.TemplateRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.TEMPLATES,
                        rows = rows,
                    )
                }
                ExportEntityType.TRANSACTIONS -> {
                    // Exact match on this app's own export header — go straight to preview just
                    // like the other four formats, instead of making the user map columns that
                    // are already known.
                    val mapping = detectColumnMapping(headers)
                    val rows = mapCsvRowsUseCase(
                        bodyRows = bodyRows,
                        mapping = mapping,
                        existingCategories = existingCategories,
                        existingCategoryGroups = existingCategoryGroups,
                        existingTransactions = existingTransactions,
                        accountNameById = existingAccounts.associate { it.id to it.name },
                        defaultCurrency = existingAccounts.firstOrNull()?.currency ?: "BDT",
                    ).map { ImportRow.TransactionRow(it) }
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.PREVIEW,
                        detectedFormat = ExportEntityType.TRANSACTIONS,
                        rows = rows,
                    )
                }
                null -> {
                    _uiState.value = ImportWizardUiState(
                        step = WizardStep.MAPPING,
                        detectedFormat = ExportEntityType.TRANSACTIONS,
                        headers = headers,
                        mapping = detectColumnMapping(headers),
                    )
                }
            }
        }
    }

    fun onMappingChange(column: ImportColumn, headerIndex: Int?) {
        _uiState.value = _uiState.value.copy(mapping = _uiState.value.mapping + (column to headerIndex))
    }

    fun confirmMapping() {
        val state = _uiState.value
        val missing = REQUIRED_COLUMNS.filter { state.mapping[it] == null }
        if (missing.isNotEmpty()) {
            _uiState.value = state.copy(
                errorMessage = "Map these columns first: ${missing.joinToString { it.label }}",
            )
            return
        }

        val accountNameById = existingAccounts.associate { it.id to it.name }
        val rows = mapCsvRowsUseCase(
            bodyRows = bodyRows,
            mapping = state.mapping,
            existingCategories = existingCategories,
            existingCategoryGroups = existingCategoryGroups,
            existingTransactions = existingTransactions,
            accountNameById = accountNameById,
            defaultCurrency = existingAccounts.firstOrNull()?.currency ?: "BDT",
        ).map { ImportRow.TransactionRow(it) }
        _uiState.value = state.copy(step = WizardStep.PREVIEW, rows = rows, errorMessage = null)
    }

    fun toggleRowAccepted(rowIndex: Int) {
        _uiState.value = _uiState.value.copy(
            rows = _uiState.value.rows.map { row ->
                if (row.rowIndex == rowIndex) row.withAccepted(!row.accepted) else row
            },
        )
    }

    fun commit() {
        val state = _uiState.value
        if (state.isCommitting) return
        viewModelScope.launch {
            _uiState.value = state.copy(isCommitting = true)
            val count = when (state.detectedFormat) {
                ExportEntityType.TRANSACTIONS -> importTransactionsUseCase(
                    state.rows.filterIsInstance<ImportRow.TransactionRow>().map { it.data },
                    existingAccounts,
                    existingCategories,
                    existingLabels,
                )
                ExportEntityType.ACCOUNTS -> importAccountsUseCase(
                    state.rows.filterIsInstance<ImportRow.AccountRow>().map { it.data },
                )
                ExportEntityType.CATEGORIES -> importCategoriesUseCase(
                    state.rows.filterIsInstance<ImportRow.CategoryRow>().map { it.data },
                    existingCategoryGroups,
                )
                ExportEntityType.LABELS -> importLabelsUseCase(
                    state.rows.filterIsInstance<ImportRow.LabelRow>().map { it.data },
                )
                ExportEntityType.BUDGETS -> importBudgetsUseCase(
                    state.rows.filterIsInstance<ImportRow.BudgetRow>().map { it.data },
                )
                ExportEntityType.TEMPLATES -> importTemplatesUseCase(
                    state.rows.filterIsInstance<ImportRow.TemplateRow>().map { it.data },
                    existingAccounts,
                    existingCategories,
                    existingLabels,
                )
            }
            _uiState.value = _uiState.value.copy(isCommitting = false, committed = true, importedCount = count)
        }
    }
}
