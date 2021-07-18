package toothpick.compiler.ksp.factory.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import toothpick.MemberInjector
import toothpick.Scope
import toothpick.compiler.ksp.common.ParamInjectionTarget
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget

private const val FACTORY_SUFFIX = "__Factory"

class FactoryCodeGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generate(injectionTarget: ConstructorInjectionTarget) {
        val packageName = injectionTarget.classDeclaration.packageName.asString()
        val classSimpleName = injectionTarget.classDeclaration.simpleName.asString()
        val generatedClassName = classSimpleName + FACTORY_SUFFIX
        val className = ClassName(packageName, classSimpleName)

        val superInterface = ClassName("toothpick", "Factory")
            .plusParameter(
                ClassName.bestGuess(injectionTarget.classDeclaration.qualifiedName!!.asString())
            )

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = generatedClassName
        ).apply {
            addType(
                TypeSpec.classBuilder(generatedClassName)
                    .addSuperinterface(superInterface)
                    .addCreateInstanceFunction(
                        className,
                        injectionTarget.parameters,
                        injectionTarget.superClassThatNeedsMemberInjection != null
                    )
                    .addMemberInjectionFor(injectionTarget.superClassThatNeedsMemberInjection)
                    .addGetTargetScope()
                    .addHasScopeAnnotation(injectionTarget.scopeName != null)
                    .addHasSingletonAnnotation(injectionTarget.hasSingletonAnnotation)
                    .addHasReleasableAnnotation(injectionTarget.hasReleasableAnnotation)
                    .addHasProvidesSingletonAnnotation(injectionTarget.hasReleasableAnnotation)
                    .addHasProvidesReleasableAnnotation(injectionTarget.hasReleasableAnnotation)
                    .build()
            )
        }.build()

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false),
            packageName = packageName,
            fileName = generatedClassName
        ).writer().use {
            fileSpec.writeTo(it)
        }
    }
}

private fun TypeSpec.Builder.addMemberInjectionFor(classThatNeedsMemberInjection: KSClassDeclaration?): TypeSpec.Builder {
    classThatNeedsMemberInjection ?: return this

    val qualifiedName = classThatNeedsMemberInjection.qualifiedName?.asString()!!
    val packageName = classThatNeedsMemberInjection.packageName.asString()
    val parametrizedClassName = ClassName.bestGuess(qualifiedName)
    val simpleName = classThatNeedsMemberInjection.simpleName.asString()

    val memberInjectorClassName = ClassName.bestGuess(
        "$packageName.${simpleName}__MemberInjector"
    )

    return this.addProperty(
        PropertySpec.builder(
            "memberInjector",
            MemberInjector::class.asClassName().plusParameter(parametrizedClassName)
        )
            .addModifiers(KModifier.PRIVATE)
            .initializer("%L()", memberInjectorClassName)
            .build()
    )
}

private fun TypeSpec.Builder.addGetTargetScope(): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("getTargetScope")
            .returns(Scope::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("scope", Scope::class)
            .addStatement("return scope")
            .build()
    )
}

private fun TypeSpec.Builder.addHasProvidesSingletonAnnotation(hasProvidesSingletonAnnotation: Boolean): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("hasProvidesSingletonAnnotation")
            .returns(Boolean::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", hasProvidesSingletonAnnotation)
            .build()
    )
}

private fun TypeSpec.Builder.addHasProvidesReleasableAnnotation(hasProvidesReleasableAnnotation: Boolean): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("hasProvidesReleasableAnnotation")
            .returns(Boolean::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", hasProvidesReleasableAnnotation)
            .build()
    )
}

private fun TypeSpec.Builder.addHasReleasableAnnotation(hasReleasableAnnotation: Boolean): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("hasReleasableAnnotation")
            .returns(Boolean::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", hasReleasableAnnotation)
            .build()
    )
}

private fun TypeSpec.Builder.addHasSingletonAnnotation(hasSingletonAnnotation: Boolean): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("hasSingletonAnnotation")
            .returns(Boolean::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", hasSingletonAnnotation)
            .build()
    )
}

private fun TypeSpec.Builder.addHasScopeAnnotation(hasScopeAnnotation: Boolean): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("hasScopeAnnotation")
            .returns(Boolean::class.java)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", hasScopeAnnotation)
            .build()
    )
}

private fun TypeSpec.Builder.addCreateInstanceFunction(
    className: ClassName,
    parameters: List<ParamInjectionTarget>,
    hasMemberInjector: Boolean
): TypeSpec.Builder {

    return this.addFunction(
        FunSpec.builder("createInstance")
            .addModifiers(KModifier.OVERRIDE)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("scope", Scope::class.java)
            .returns(className)
            .apply {
                parameters.forEachIndexed { index, param ->
                    addStatement(
                        "val param%L = scope.%L(%L::class.java)",
                        index,
                        param.kind.toScopeGetMethod(),
                        param.memberType
                    )
                }
            }
            .addStatement(
                "val instance = %L(%L)",
                className.simpleName,
                parameters.indices.joinToString(", ") { "param$it" }
            )
            .apply {
                if (hasMemberInjector) {
                    addStatement("memberInjector.inject(instance, scope)")
                }
            }
            .addStatement("return instance")
            .build()
    )
}

private fun ParamInjectionTarget.Kind.toScopeGetMethod(): String {
    return when (this) {
        ParamInjectionTarget.Kind.INSTANCE -> "getInstance"
        ParamInjectionTarget.Kind.LAZY -> "getLazy"
        ParamInjectionTarget.Kind.PROVIDER -> "getProvider"
    }
}
