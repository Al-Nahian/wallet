package com.example.wallet.feature.importexport

import android.net.Uri

object ImportExportRoutes {
    const val ENTRY = "importexport"
    const val URI_ARG = "uri"
    const val WIZARD_PATTERN = "importexport/wizard/{$URI_ARG}"

    fun wizard(uri: String): String = "importexport/wizard/${Uri.encode(uri)}"
}
