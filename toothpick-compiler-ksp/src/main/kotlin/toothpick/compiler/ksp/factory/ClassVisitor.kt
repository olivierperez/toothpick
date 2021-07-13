package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import toothpick.compiler.ksp.factory.generators.FactoryCodeGenerator
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget

class ClassVisitor(
    private val logger: KSPLogger,
    private val factoryCodeGenerator: FactoryCodeGenerator
) : KSVisitorVoid() {

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        logger.info("#OPZ# Visiting function declaration: $function")
        val classDeclaration = function.parentDeclaration as KSClassDeclaration

        function.isPublic()
                || function.isInternal()
                || error("@Inject constructors must be public in class ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        classDeclaration.isPublic()
                || classDeclaration.isInternal()
                || error("@Inject constructors are not allowed in private classe ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        visitClassDeclaration(classDeclaration, data)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val outputFilename = classDeclaration.simpleName.getShortName()
        val packageName = classDeclaration.packageName.asString()
        logger.info("#OPZ# Visiting class declaration. package:$packageName, filename:$outputFilename")

        val constructorInjectionTarget = ConstructorInjectionTarget(
            classDeclaration = classDeclaration,
            scopeName = classDeclaration.getScopeName(),
            hasSingletonAnnotation = false,
            hasReleasableAnnotation = false,
            hasProvidesSingletonInScopeAnnotation = false,
            hasProvidesReleasableAnnotation = false,
            superClassThatNeedsMemberInjection = null
        )

        factoryCodeGenerator.generate(constructorInjectionTarget)
    }

    override fun visitNode(node: KSNode, data: Unit) {
        logger.info("#OPZ# Visiting node: $node")
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        logger.info("#OPZ# Visiting annotated: $annotated")
    }

}

private fun KSClassDeclaration.getScopeName(): String? {
    return null
}
