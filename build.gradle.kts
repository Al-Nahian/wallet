plugins {
    id("com.android.application") version "8.6.1" apply false
    // Kotlin compiler pinned to 2.1.21, not the newer 2.2.x the backdrop library's stdlib
    // dependency nominally wants: Kotlin 2.2 emits @Metadata in a format (2.2.0) that Dagger/Hilt
    // 2.51.1's bundled kotlinx-metadata-jvm reader can't parse (max supported 2.1.0), which fails
    // kapt outright. 2.1.21 still resolves a 2.2.21 kotlin-stdlib jar at the dependency-graph
    // level (that requirement is a floor, not a strict pin) — only the compiler/metadata version
    // actually needs to stay at 2.1.
    id("org.jetbrains.kotlin.android") version "2.1.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    // Replaces kapt for Hilt/Room: kapt's javac-stub approach kept hitting Kotlin-2.x-specific
    // bugs (a Dagger XProcessing NullPointerException, a "cannot access" stub-generation error)
    // that don't apply to KSP, since it operates on Kotlin symbols directly with no Java stub step.
    id("com.google.devtools.ksp") version "2.1.21-2.0.2" apply false
}
