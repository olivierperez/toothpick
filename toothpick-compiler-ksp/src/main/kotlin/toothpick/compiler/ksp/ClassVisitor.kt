package toothpick.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec

class ClassVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : KSVisitorVoid() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        logger.info("#OPZ# Visiting function declaration: $function")
        val classDeclaration = function.parentDeclaration as  KSClassDeclaration
        visitClassDeclaration(classDeclaration, data)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        logger.info("#OPZ# Visiting class declaration: $classDeclaration")
        val fileSpec = FileSpec.builder(
            packageName = "fr.o80",
            fileName = "OPZ.kt"
        ).apply {
            addFunction(
                FunSpec.builder("doSomething")
                    .receiver(ClassName.bestGuess("java.lang.String"))
                    .addStatement("val sum = 1 + 1")
                    .build()
            )
        }.build()

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false),
            packageName = "fr.o80",
            fileName = "OPZ.kt"
        ).use { output ->
            output.writer().use {
                fileSpec.writeTo(it)
            }
        }
    }

    override fun visitNode(node: KSNode, data: Unit) {
        logger.info("#OPZ# Visiting node: $node")
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        logger.info("#OPZ# Visiting annotated: $annotated")
    }

}
