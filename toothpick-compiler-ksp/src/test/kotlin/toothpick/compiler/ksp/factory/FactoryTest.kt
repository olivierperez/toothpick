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

              public override fun getTargetScope(scope: Scope): Scope = scope

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
    @DisplayName("with empty internal constructor")
    fun testEmptyConstructor_shouldWork_whenConstructorIsInternal() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestEmptyConstructor @Inject internal constructor()
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

              public override fun getTargetScope(scope: Scope): Scope = scope

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

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("@Inject constructors must be public in class test.TestProtectedConstructor"),
            "Error message not found!"
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

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("@Inject constructors must be public in class test.TestPrivateConstructor"),
            "Error message not found!"
        )
    }

    @Test
    @DisplayName("with private class")
    fun testPrivateClass_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestPrivateClass.kt",
            contents = """
                package test

                import javax.inject.Inject

                private class TestPrivateClass @Inject constructor()
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("@Inject constructors are not allowed in private classe test.TestPrivateClass"),
            "Error message not found!"
        )
    }

    @Test
    @DisplayName("with internal class")
    fun testInternalClass_shouldWork_whenConstructorIsPublic() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                internal class TestEmptyConstructor @Inject constructor()
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

              public override fun getTargetScope(scope: Scope): Scope = scope

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
    @DisplayName("with 2 injected constructors")
    fun testTwoInjectedConstructors_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestTwoInjectedConstructors.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestTwoInjectedConstructors @Inject constructor() {
                  @Inject
                  constructor(message: String): this()
                }
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("Class test.TestTwoInjectedConstructors cannot have more than one @Inject annotated constructor."),
            "Error message not found!"
        )
    }

    @Test
    @DisplayName("with 2 constructor but only 1 injected")
    fun testOneOnTwoConstructorIsInjected_shouldWork() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestOnlyOneInjectedConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestOnlyOneInjectedConstructor @Inject constructor() {
                    constructor(message: String): this()
                }
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
            
            public class TestOnlyOneInjectedConstructor__Factory : Factory<TestOnlyOneInjectedConstructor> {
              public override fun createInstance(scope: Scope): TestOnlyOneInjectedConstructor {
                val instance = TestOnlyOneInjectedConstructor()
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestOnlyOneInjectedConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with one member injected")
    fun testAClassThatNeedsInjectionForMember_shouldUseAMemberInjector() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestWithMemberInjection.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestWithMemberInjection @Inject constructor() {
                  @Inject
                  lateinit var message: String
                }
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
            import toothpick.MemberInjector
            import toothpick.Scope
            
            public class TestWithMemberInjection__Factory : Factory<TestWithMemberInjection> {
              private val memberInjector: MemberInjector<TestWithMemberInjection> =
                  test.TestWithMemberInjection__MemberInjector()

              public override fun createInstance(scope: Scope): TestWithMemberInjection {
                val instance = TestWithMemberInjection()
                memberInjector.inject(instance, scope)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestWithMemberInjection__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with one method injected")
    fun testAClassThatNeedsInjectionForMethod_shouldUseAMemberInjector() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestWithMethodInjection.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestWithMethodInjection @Inject constructor() {
                  @Inject
                  fun countLetters(message: String) {}
                }
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
            import toothpick.MemberInjector
            import toothpick.Scope
            
            public class TestWithMethodInjection__Factory : Factory<TestWithMethodInjection> {
              private val memberInjector: MemberInjector<TestWithMethodInjection> =
                  test.TestWithMethodInjection__MemberInjector()

              public override fun createInstance(scope: Scope): TestWithMethodInjection {
                val instance = TestWithMethodInjection()
                memberInjector.inject(instance, scope)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestWithMethodInjection__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with inheritance of a class with one member injected")
    fun testAClassThatInheritFromAnotherClassThatNeedsInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestAClassThatNeedsInjection.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestAClassThatNeedsInjection @Inject constructor(): SuperClassThatNeedsInjection()
                open class SuperClassThatNeedsInjection {
                  @Inject
                  lateinit var message: String
                }
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
            import toothpick.MemberInjector
            import toothpick.Scope
            
            public class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              private val memberInjector: MemberInjector<SuperClassThatNeedsInjection> =
                  test.SuperClassThatNeedsInjection__MemberInjector()

              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val instance = TestAClassThatNeedsInjection()
                memberInjector.inject(instance, scope)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestAClassThatNeedsInjection__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with non-empty public constructor")
    fun testNonEmptyConstructor_shouldInjectParameters() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestNonEmptyConstructor @Inject constructor(message: String, value: Int)
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
            
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val param0 = scope.getInstance(String::class.java)
                val param1 = scope.getInstance(Int::class.java)
                val instance = TestNonEmptyConstructor(param0, param1)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestNonEmptyConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with non-empty public constructor with Lazy")
    fun testNonEmptyConstructorWithLazy_shouldInjectParameters() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject
                import toothpick.Lazy

                class TestNonEmptyConstructor @Inject constructor(message: Lazy<String>, value: Int)
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
            
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val param0 = scope.getLazy(String::class.java)
                val param1 = scope.getInstance(Int::class.java)
                val instance = TestNonEmptyConstructor(param0, param1)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestNonEmptyConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with non-empty public constructor with Provider")
    fun testNonEmptyConstructorWithProvier_shouldInjectParameters() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject
                import javax.inject.Provider

                class TestNonEmptyConstructor @Inject constructor(message: Provider<String>, value: Int)
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
            
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val param0 = scope.getProvider(String::class.java)
                val param1 = scope.getInstance(Int::class.java)
                val instance = TestNonEmptyConstructor(param0, param1)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestNonEmptyConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with non-empty public constructor with Generic")
    fun testNonEmptyConstructorWithGeneric_shouldInjectParameters() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject

                class TestNonEmptyConstructor @Inject constructor(messages: List<String>, value: Int)
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
            
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val param0 = scope.getInstance(List::class.java)
                val param1 = scope.getInstance(Int::class.java)
                val instance = TestNonEmptyConstructor(param0, param1)
                return instance
              }

              public override fun getTargetScope(scope: Scope): Scope = scope

              public override fun hasScopeAnnotation(): Boolean = false

              public override fun hasSingletonAnnotation(): Boolean = false

              public override fun hasReleasableAnnotation(): Boolean = false

              public override fun hasProvidesSingletonAnnotation(): Boolean = false

              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            
            """.trimIndent(),
            File(
                compilationResult.outputDirectory,
                "../ksp/sources/kotlin/test/TestNonEmptyConstructor__Factory.kt"
            ).readText()
        )
    }

    @Test
    @DisplayName("with non-empty public constructor with Provider of generic")
    fun testNonEmptyConstructorWithProviderGeneric_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject
                import toothpick.Lazy

                class TestNonEmptyConstructor @Inject constructor(messages: Lazy<List<String>>)
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("Lazy/Provider TestNonEmptyConstructor.messages is not a valid in <init>. Lazy/Provider cannot be used on generic types."),
            "Error message not found!"
        )
    }

    @Test
    @DisplayName("with non-empty public constructor with Lazy of generic")
    fun testNonEmptyConstructorWithLazyGeneric_shouldNotAllowInjection() {
        // Given
        val kotlinSource = SourceFile.kotlin(
            trimIndent = true,
            name = "TestNonEmptyConstructor.kt",
            contents = """
                package test

                import javax.inject.Inject
                import javax.inject.Provider

                class TestNonEmptyConstructor @Inject constructor(messages: Provider<List<String>>)
                """
        )

        // When
        val compilationResult = compile(temporaryFolder, kotlinSource)

        // Then

        assertEquals(
            KotlinCompilation.ExitCode.COMPILATION_ERROR,
            compilationResult.exitCode,
            "Compilation did not failed as expected!"
        )
        assertTrue(
            compilationResult.kspGeneratedSources().foldersAreEmpty(),
            "Nothing should be generated!"
        )
        assertTrue(
            compilationResult.messages.contains("Lazy/Provider TestNonEmptyConstructor.messages is not a valid in <init>. Lazy/Provider cannot be used on generic types."),
            "Error message not found!"
        )
    }
}
