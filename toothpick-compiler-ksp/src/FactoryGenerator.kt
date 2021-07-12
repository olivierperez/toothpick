/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.ksp.factory.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import toothpick.Factory
import toothpick.Scope
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget
import javax.inject.Singleton
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

/**
 * Generates a [Factory] for a given [ConstructorInjectionTarget]. Typically a factory
 * is created for a class a soon as it contains an [javax.inject.Inject] annotated
 * constructor. See Optimistic creation of factories in TP wiki.
 */
class FactoryGenerator(
    private val constructorInjectionTarget: ConstructorInjectionTarget,
    types: Types?
) : CodeGenerator {

    fun brewJava(): String {
        // Interface to implement
        val className: ClassName = ClassName.bestGuess(constructorInjectionTarget.builtClass.toString())
        val parameterizedTypeName: ParameterizedTypeName = Factory::class.java.parameterizedBy(constructorInjectionTarget.builtClass)

        // Build class
        val factoryTypeSpec: TypeSpec.Builder =
            TypeSpec.classBuilder(getGeneratedSimpleClassName(constructorInjectionTarget.builtClass).toString() + FACTORY_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(parameterizedTypeName)
        emitSuperMemberInjectorFieldIfNeeded(factoryTypeSpec)
        emitCreateInstance(factoryTypeSpec)
        emitGetTargetScope(factoryTypeSpec)
        emitHasScopeAnnotation(factoryTypeSpec)
        emitHasSingletonAnnotation(factoryTypeSpec)
        emitHasReleasableAnnotation(factoryTypeSpec)
        emitHasProvidesSingletonAnnotation(factoryTypeSpec)
        emitHasProvidesReleasableAnnotation(factoryTypeSpec)
        val javaFile: JavaFile = JavaFile.builder(className.packageName(), factoryTypeSpec.build()).build()
        return javaFile.toString()
    }

    private fun emitSuperMemberInjectorFieldIfNeeded(scopeMemberTypeSpec: TypeSpec.Builder) {
        if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
            val superTypeThatNeedsInjection: ClassName =
                ClassName.bestGuess(constructorInjectionTarget.superClassThatNeedsMemberInjection)
            val memberInjectorSuperParameterizedTypeName: ParameterizedTypeName = ParameterizedTypeName.get(
                ClassName.bestGuess(MemberInjector::class.java), superTypeThatNeedsInjection
            )
            val superMemberInjectorField: FieldSpec.Builder = FieldSpec.builder(
                memberInjectorSuperParameterizedTypeName, "memberInjector", Modifier.PRIVATE
            ) // TODO use proper typing here
                .initializer(
                    "new \$L__MemberInjector()",
                    getGeneratedFQNClassName(
                        constructorInjectionTarget.superClassThatNeedsMemberInjection
                    )
                )
            scopeMemberTypeSpec.addField(superMemberInjectorField.build())
        }
    }

    val fqcn: String
        get() = getGeneratedFQNClassName(constructorInjectionTarget.builtClass).toString() + FACTORY_SUFFIX

    private fun emitCreateInstance(builder: TypeSpec.Builder) {
        val className: ClassName = ClassName.bestGuess(constructorInjectionTarget.builtClass)
        val createInstanceBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("createInstance")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.bestGuess(Scope::class.java), "scope")
            .returns(className)

        // change the scope to target scope so that all dependencies are created in the target scope
        // and the potential injection take place in the target scope too
        if (!constructorInjectionTarget.parameters.isEmpty()
            || constructorInjectionTarget.superClassThatNeedsMemberInjection != null
        ) {
            // We only need it when the constructor contains parameters or dependencies
            createInstanceBuilder.addStatement("scope = getTargetScope(scope)")
        }
        val localVarStatement = StringBuilder("")
        val simpleClassName: String = getSimpleClassName(className)
        localVarStatement.append(simpleClassName).append(" ")
        var varName = "" + Character.toLowerCase(className.simpleName().charAt(0))
        varName += className.simpleName().substring(1)
        localVarStatement.append(varName).append(" = ")
        localVarStatement.append("new ")
        localVarStatement.append(simpleClassName).append("(")
        var counter = 1
        var prefix = ""
        val codeBlockBuilder: CodeBlock.Builder = CodeBlock.builder()
        if (constructorInjectionTarget.throwsThrowable) {
            codeBlockBuilder.beginControlFlow("try")
        }
        for (paramInjectionTarget in constructorInjectionTarget.parameters) {
            val invokeScopeGetMethodWithNameCodeBlock: CodeBlock =
                getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget)
            val paramName = "param" + counter++
            codeBlockBuilder.add("\$T \$L = scope.", getParamType(paramInjectionTarget), paramName)
            codeBlockBuilder.add(invokeScopeGetMethodWithNameCodeBlock)
            codeBlockBuilder.add(";")
            codeBlockBuilder.add(LINE_SEPARATOR)
            localVarStatement.append(prefix)
            localVarStatement.append(paramName)
            prefix = ", "
        }
        localVarStatement.append(")")
        codeBlockBuilder.addStatement(localVarStatement.toString())
        if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
            codeBlockBuilder.addStatement("memberInjector.inject(\$L, scope)", varName)
        }
        codeBlockBuilder.addStatement("return \$L", varName)
        if (constructorInjectionTarget.throwsThrowable) {
            codeBlockBuilder.nextControlFlow("catch(\$L ex)", ClassName.bestGuess(Throwable::class.java))
            codeBlockBuilder.addStatement("throw new \$L(ex)", ClassName.bestGuess(RuntimeException::class.java))
            codeBlockBuilder.endControlFlow()
        }
        createInstanceBuilder.addCode(codeBlockBuilder.build())
        builder.addMethod(createInstanceBuilder.build())
    }

    private fun emitGetTargetScope(builder: TypeSpec.Builder) {
        val getParentScopeCodeBlockBuilder: CodeBlock.Builder = parentScopeCodeBlockBuilder
        val getScopeBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("getTargetScope")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.bestGuess(Scope::class.java), "scope")
            .returns(ClassName.bestGuess(Scope::class.java))
            .addStatement("return scope\$L", getParentScopeCodeBlockBuilder.build().toString())
        builder.addMethod(getScopeBuilder.build())
    }

    private fun emitHasScopeAnnotation(builder: TypeSpec.Builder) {
        val scopeName: String = constructorInjectionTarget.scopeName
        val hasScopeAnnotation = scopeName != null
        val hasScopeAnnotationBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("hasScopeAnnotation")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement("return \$L", hasScopeAnnotation)
        builder.addMethod(hasScopeAnnotationBuilder.build())
    }

    private fun emitHasSingletonAnnotation(builder: TypeSpec.Builder) {
        val hasSingletonAnnotation: Boolean = constructorInjectionTarget.hasSingletonAnnotation
        val hasScopeAnnotationBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("hasSingletonAnnotation")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement("return \$L", hasSingletonAnnotation)
        builder.addMethod(hasScopeAnnotationBuilder.build())
    }

    private fun emitHasReleasableAnnotation(builder: TypeSpec.Builder) {
        val hasReleasableAnnotation: Boolean = constructorInjectionTarget.hasReleasableAnnotation
        val hasScopeAnnotationBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("hasReleasableAnnotation")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement("return \$L", hasReleasableAnnotation)
        builder.addMethod(hasScopeAnnotationBuilder.build())
    }

    private fun emitHasProvidesSingletonAnnotation(builder: TypeSpec.Builder) {
        val hasProducesSingletonBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("hasProvidesSingletonAnnotation")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement(
                "return \$L", constructorInjectionTarget.hasProvidesSingletonInScopeAnnotation
            )
        builder.addMethod(hasProducesSingletonBuilder.build())
    }

    private fun emitHasProvidesReleasableAnnotation(builder: TypeSpec.Builder) {
        val hasProducesSingletonBuilder: MethodSpec.Builder =
            MethodSpec.methodBuilder("hasProvidesReleasableAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return \$L", constructorInjectionTarget.hasProvidesReleasableAnnotation)
        builder.addMethod(hasProducesSingletonBuilder.build())
    }

    // there is no scope name or the current @Scoped annotation.
    private val parentScopeCodeBlockBuilder: CodeBlock.Builder
        private get() {
            val getParentScopeCodeBlockBuilder: CodeBlock.Builder = CodeBlock.builder()
            val scopeName: String = constructorInjectionTarget.scopeName
            if (scopeName != null) {
                // there is no scope name or the current @Scoped annotation.
                if (Singleton::class.java.name == scopeName) {
                    getParentScopeCodeBlockBuilder.add(".getRootScope()")
                } else {
                    getParentScopeCodeBlockBuilder.add(".getParentScope(\$L.class)", scopeName)
                }
            }
            return getParentScopeCodeBlockBuilder
        }

    companion object {
        private const val FACTORY_SUFFIX = "__Factory"
    }

    init {
        this.constructorInjectionTarget = constructorInjectionTarget
    }
}