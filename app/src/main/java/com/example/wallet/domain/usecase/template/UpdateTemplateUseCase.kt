package com.example.wallet.domain.usecase.template

import com.example.wallet.domain.model.Template
import com.example.wallet.domain.repository.TemplateRepository
import javax.inject.Inject

class UpdateTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(
        templateId: String,
        name: String,
        accountId: String?,
        categoryId: String?,
        labelId: String?,
        payee: String?,
        place: String?,
    ): Result<Template> {
        val existing = templateRepository.getTemplate(templateId)
            ?: return Result.failure(TemplateValidationException(TemplateError.TemplateNotFound))
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

        val updated = existing.copy(
            name = cleanName,
            accountId = accountId,
            categoryId = categoryId,
            labelId = labelId,
            payee = payee?.trim()?.takeIf { it.isNotEmpty() },
            place = place?.trim()?.takeIf { it.isNotEmpty() },
        )
        templateRepository.update(updated)
        return Result.success(updated)
    }
}
