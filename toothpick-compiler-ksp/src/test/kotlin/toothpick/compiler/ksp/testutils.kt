package toothpick.compiler.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import toothpick.compiler.ksp.factory.FactorySymbolProcessorProvider
import java.io.File

internal fun compile(temporaryFolder: File, vararg source: SourceFile) = KotlinCompilation().apply {
    sources = source.toList()
    symbolProcessorProviders = listOf(FactorySymbolProcessorProvider())
    workingDir = temporaryFolder
    inheritClassPath = true
    verbose = false
}.compile()

internal fun KotlinCompilation.Result.kspGeneratedSources(): List<File> {
    val kspWorkingDir = workingDir.resolve("ksp")
    val kspGeneratedDir = kspWorkingDir.resolve("sources")
    val kotlinGeneratedDir = kspGeneratedDir.resolve("kotlin")
    val javaGeneratedDir = kspGeneratedDir.resolve("java")
    return kotlinGeneratedDir.walk().toList() +
            javaGeneratedDir.walk().toList()
}

internal fun List<File>.foldersAreEmpty(): Boolean {
    return all { it.list()?.isEmpty() ?: false }
}

internal val KotlinCompilation.Result.workingDir: File
    get() = checkNotNull(outputDirectory.parentFile)
