package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import toothpick.compiler.ksp.common.ParamInjectionTarget
import toothpick.compiler.ksp.common.containsInject
import toothpick.compiler.ksp.common.isMethod
import toothpick.compiler.ksp.common.superClass
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

        _targets += ConstructorInjectionTarget(
            classDeclaration = classDeclaration,
            constructorDeclaration = function,
            scopeName = classDeclaration.getScopeName(),
            hasSingletonAnnotation = false,
            hasReleasableAnnotation = false,
            hasProvidesSingletonInScopeAnnotation = false,
            hasProvidesReleasableAnnotation = false,
            superClassThatNeedsMemberInjection = classDeclaration.getMostDirectSuperClassWithInjectedMembers(),
            parameters = function.parameters.toParameterTarget()
        )
    }

}

private fun List<KSValueParameter>.toParameterTarget(): List<ParamInjectionTarget> {
    return this.map {
        ParamInjectionTarget(
            memberType = it.type,
            memberName = it.name!!.asString(),
            kind = ParamInjectionTarget.Kind.INSTANCE,
            kindParamClass = it.type,
            name = it.annotatedName
        )
    }
}

private val KSValueParameter.annotatedName: String?
    get() = null

private fun KSClassDeclaration.getMostDirectSuperClassWithInjectedMembers(): KSClassDeclaration? {
    var klass: KSClassDeclaration? = this
    do {
        if (klass!!.declarations.any { (it is KSPropertyDeclaration || (it.isMethod())) && it.annotations.containsInject() }) {
            return klass
        }

        klass = klass.superClass
    } while (klass != null)

    return null
}

private fun KSClassDeclaration.getScopeName(): String? {
    return null
}
