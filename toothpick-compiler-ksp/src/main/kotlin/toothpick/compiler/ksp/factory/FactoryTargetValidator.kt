package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget

class FactoryTargetValidator {

    fun check(target: ConstructorInjectionTarget) {
        val constructor = target.constructorDeclaration
        val classDeclaration = target.classDeclaration

        constructor.isPublic()
                || constructor.isInternal()
                || error("@Inject constructors must be public in class ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        classDeclaration.isPublic()
                || classDeclaration.isInternal()
                || error("@Inject constructors are not allowed in private classe ${classDeclaration.packageName.asString()}.${classDeclaration.simpleName.asString()}")

        target.isSingleInjectedConstructor() ||
            error("Class ${target.classDeclaration.packageName.asString()}.${target.classDeclaration.simpleName.asString()} cannot have more than one @Inject annotated constructor.")
    }

    private fun ConstructorInjectionTarget.isSingleInjectedConstructor() : Boolean {
        val injectedConstructorsCount = classDeclaration.getConstructors()
            .count { constructor -> constructor.isInjected() }

        return injectedConstructorsCount == 1
    }
}

private fun KSFunctionDeclaration.isInjected(): Boolean {
    // TODO Find another way to check to presence of javax.inject.Inject
    return annotations.any { annotation -> annotation.shortName.asString() == "Inject" }
}

