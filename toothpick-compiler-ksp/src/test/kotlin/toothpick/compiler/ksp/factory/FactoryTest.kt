package toothpick.compiler.ksp.factory

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import toothpick.compiler.ksp.compile
import toothpick.compiler.ksp.foldersAreEmpty
import toothpick.compiler.ksp.kspGeneratedSources
import java.io.File

@DisplayName("When generating Factories")
class FactoryTest {

    @TempDir
    lateinit var temporaryFolder: File

    @Test
    @DisplayName("with empty public constructor")
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
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestEmptyConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with empty protected constructor")
    fun testProtectedConstructor_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestProtectedConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                open class TestProtectedConstructor @Inject protected constructor()
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilationResult.exitCode)
        assertTrue(compilationResult.kspGeneratedSources().foldersAreEmpty())
        assertTrue(
            compilationResult.messages.contains("@Inject constructors must be public in class test.TestProtectedConstructor")
        )
    }

    @Test
    @DisplayName("with empty private constructor")
    fun testPrivateConstructor_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestPrivateConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestPrivateConstructor @Inject private constructor()
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilationResult.exitCode)
        assertTrue(compilationResult.kspGeneratedSources().foldersAreEmpty())
        assertTrue(
            compilationResult.messages.contains("@Inject constructors must be public in class test.TestPrivateConstructor")
        )
    }
}
