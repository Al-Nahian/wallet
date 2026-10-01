package com.expensetracker.wallet.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.usecase.category.CategoryValidationException
import com.expensetracker.wallet.domain.usecase.category.CreateCategoryUseCase
import com.expensetracker.wallet.domain.usecase.category.DeleteCategoryUseCase
import com.expensetracker.wallet.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryGroupUi(val group: CategoryGroup, val categories: List<Category>)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    val groups: StateFlow<List<CategoryGroupUi>> = combine(
        categoryRepository.observeGroups(),
        categoryRepository.observeCategories(),
    ) { groups, categories ->
        groups
            .sortedBy { it.sortOrder }
            .map { group ->
                CategoryGroupUi(group, categories.filter { it.groupId == group.id }.sortedBy { it.sortOrder })
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun createCategory(groupId: String, name: String) {
        viewModelScope.launch {
            createCategoryUseCase(groupId, name).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun renameCategory(categoryId: String, name: String) {
        viewModelScope.launch {
            updateCategoryUseCase(categoryId, name).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            deleteCategoryUseCase(categoryId).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        (error as? CategoryValidationException)?.error?.userMessage
            ?: "Something went wrong. Please try again."
}
