package io.edenx.androidpark

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * Type-safe `libs.xxx` accessors are generated per build script and do not
 * exist inside a binary convention plugin, so we go through the catalog by
 * name. "libs" resolves to the catalog declared in
 * build-logic/settings.gradle.kts, which points at gradle/libs.versions.toml.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { error("No library '$alias' in libs.versions.toml") }

internal fun VersionCatalog.bundle(alias: String): Provider<ExternalModuleDependencyBundle> =
    findBundle(alias).orElseThrow { error("No bundle '$alias' in libs.versions.toml") }

internal fun VersionCatalog.versionInt(alias: String): Int =
    findVersion(alias).orElseThrow { error("No version '$alias' in libs.versions.toml") }
        .requiredVersion.toInt()

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).orElseThrow { error("No plugin '$alias' in libs.versions.toml") }
        .get().pluginId
