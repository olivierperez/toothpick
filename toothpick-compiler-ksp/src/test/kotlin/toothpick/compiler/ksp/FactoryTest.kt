package toothpick.compiler.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import toothpick.compiler.ksp.factory.FactorySymbolProcessorProvider
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

@DisplayName("When generating Factories")
class FactoryTest {

    @TempDir
    lateinit var temporaryFolder: File

    @Test
    @DisplayName("with public empty constructor")
    fun testEmptyConstructor_shouldWork_whenConstructorIsPublic() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestEmptyConstructor @Inject constructor()
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then
        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode)
        assertTrue(compilationResult.kspGeneratedSources().isNotEmpty())
        assertEquals(
            """
            package test
            
            import kotlin.Boolean
            import toothpick.Factory
            import toothpick.Scope
            
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val instance = TestEmptyConstructor()
                return instance
              }

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(compilationResult.outputDirectory, "../ksp/sources/kotlin/test/TestEmptyConstructor__Factory.kt").readText()
        )
    }
}
