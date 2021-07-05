package toothpick.compiler.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private fun compile(temporaryFolder: File, vararg source: SourceFile) = KotlinCompilation().apply {
    sources = source.toList()
    symbolProcessorProviders = listOf(FactorySymbolProcessorProvider())
    workingDir = temporaryFolder
    inheritClassPath = true
    verbose = false
}.compile()

private fun KotlinCompilation.Result.kspGeneratedSources(): List<File> {
    val kspWorkingDir = workingDir.resolve("ksp")
    val kspGeneratedDir = kspWorkingDir.resolve("sources")
    val kotlinGeneratedDir = kspGeneratedDir.resolve("kotlin")
    val javaGeneratedDir = kspGeneratedDir.resolve("java")
    return kotlinGeneratedDir.walk().toList() +
            javaGeneratedDir.walk().toList()
}

private val KotlinCompilation.Result.workingDir: File
    get() = checkNotNull(outputDirectory.parentFile)

class KspTest {
    @TempDir
    lateinit var temporaryFolder: File

    @Test
    @DisplayName("Toto")
    fun toto() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "FooInjectable.kt",
            contents = """
                package com.tests.summable
                
                import javax.inject.Inject
                
                class FooInjectable @Inject constructor(
                    val bar: Int,
                    val baz: Int
                )
                """
        )

        // When
        val compilationResult = compile(File("E:\\progs\\android\\toothpick\\toothpick-compiler-ksp\\opz"), kotlinSource)

        // Then
        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode)
        assertTrue(compilationResult.kspGeneratedSources().isNotEmpty())
        assertEquals(
            """
            package com.tests.summable
            
            import kotlin.Int
            
            public fun FooSummable.sumInts(): Int {
              val sum = bar + baz
              return sum
            }""",
            compilationResult.outputDirectory
        )
    }
}
