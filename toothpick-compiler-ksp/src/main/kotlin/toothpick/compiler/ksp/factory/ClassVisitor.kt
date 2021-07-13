package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget

class ClassVisitor(
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val _targets = mutableListOf<ConstructorInjectionTarget>()
    val targets: List<ConstructorInjectionTarget>
        get() = _targets

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        logger.info("#OPZ# Visiting function declaration: $function")
        val classDeclaration = function.parentDeclaration as KSClassDeclaration

        function.isPublic()
                || function.isInternal()
                || error("@Inject constructors must be public in class ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        classDeclaration.isPublic()
                || classDeclaration.isInternal()
                || error("@Inject constructors are not allowed in private classe ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        _targets += ConstructorInjectionTarget(
            classDeclaration = classDeclaration,
            scopeName = classDeclaration.getScopeName(),
            hasSingletonAnnotation = false,
            hasReleasableAnnotation = false,
            hasProvidesSingletonInScopeAnnotation = false,
            hasProvidesReleasableAnnotation = false,
            superClassThatNeedsMemberInjection = null
        )
    }

}

private fun KSClassDeclaration.getScopeName(): String? {
    return null
}
