package com.example.wallet.domain.usecase.template

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.Template
import com.example.wallet.domain.repository.TemplateRepository
import javax.inject.Inject

class CreateTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(
        name: String,
        accountId: String?,
        categoryId: String?,
        labelId: String?,
        payee: String?,
        place: String?,
    ): Result<Template> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(TemplateValidationException(TemplateError.NameRequired))
        }
        if (accountId.isNullOrBlank()) {
            return Result.failure(TemplateValidationException(TemplateError.AccountRequired))
        }
        if (categoryId.isNullOrBlank()) {
            return Result.failure(TemplateValidationException(TemplateError.CategoryRequired))
        }
        if (labelId.isNullOrBlank()) {
            return Result.failure(TemplateValidationException(TemplateError.LabelRequired))
        }

        val template = Template(
            id = newId(),
            name = cleanName,
            accountId = accountId,
            categoryId = categoryId,
            labelId = labelId,
            payee = payee?.trim()?.takeIf { it.isNotEmpty() },
            place = place?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = System.currentTimeMillis(),
        )
        templateRepository.create(template)
        return Result.success(template)
    }
}
