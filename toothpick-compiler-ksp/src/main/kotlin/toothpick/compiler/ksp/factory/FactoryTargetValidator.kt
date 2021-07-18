package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import toothpick.compiler.ksp.common.ParamInjectionTarget
import toothpick.compiler.ksp.common.isInjected
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget

class FactoryTargetValidator {

    fun check(target: ConstructorInjectionTarget) {
        val constructor = target.constructorDeclaration
        val classDeclaration = target.classDeclaration
        val parameters = target.parameters

        constructor.isPublic()
                || constructor.isInternal()
                || error("@Inject constructors must be public in class ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        classDeclaration.isPublic()
                || classDeclaration.isInternal()
                || error("@Inject constructors are not allowed in private classe ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        target.isSingleInjectedConstructor() ||
                error("Class ${target.classDeclaration.packageName.asString()}.${target.classDeclaration.simpleName.asString()} cannot have more than one @Inject annotated constructor.")

        parameters.forEach { param ->
            param.kind == ParamInjectionTarget.Kind.INSTANCE || param.memberType.resolve().arguments.isEmpty() ||
                    error("Lazy/Provider ${classDeclaration.simpleName.asString()}.${param.name} is not a valid in <init>. Lazy/Provider cannot be used on generic types.")
        }
    }

    private fun ConstructorInjectionTarget.isSingleInjectedConstructor(): Boolean {
        val injectedConstructorsCount = classDeclaration.getConstructors()
            .count { constructor -> constructor.isInjected() }

        return injectedConstructorsCount == 1
    }
}
