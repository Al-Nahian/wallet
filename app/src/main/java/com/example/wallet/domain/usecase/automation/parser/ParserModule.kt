package com.example.wallet.domain.usecase.automation.parser

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/** Registers every [SmsParser] implementation into the `Set<SmsParser>` [SmsParserRegistry]
 * consumes — adding a new provider parser only ever means implementing [SmsParser] and adding
 * one `@Binds` here, never touching the registry itself. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {
    @Binds
    @IntoSet
    abstract fun bindBkashParser(impl: BkashParser): SmsParser

    @Binds
    @IntoSet
    abstract fun bindGenericBankParser(impl: GenericBankParser): SmsParser
}
