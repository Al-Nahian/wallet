package com.expensetracker.wallet.domain.usecase.template

import com.expensetracker.wallet.domain.repository.TemplateRepository
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(templateId: String): Result<Unit> {
        templateRepository.delete(templateId)
        return Result.success(Unit)
    }
}
