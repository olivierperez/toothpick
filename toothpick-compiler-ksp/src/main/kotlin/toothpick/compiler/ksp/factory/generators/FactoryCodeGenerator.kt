package toothpick.compiler.ksp.factory.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import toothpick.Scope
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
                    .addCreateInstanceFunction(className)
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

private fun TypeSpec.Builder.addCreateInstanceFunction(className: ClassName): TypeSpec.Builder {
    return this.addFunction(
        FunSpec.builder("createInstance")
            .addModifiers(KModifier.OVERRIDE)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("scope", Scope::class.java)
            .returns(className)
            .addStatement("val instance = %L()", className.simpleName)
            .addStatement("return instance")
            .build()
    )
}
