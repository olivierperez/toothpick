package toothpick.compiler.ksp.common

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

fun KSFunctionDeclaration.isInjected(): Boolean {
    return annotations.containsInject()
}

fun Sequence<KSAnnotation>.containsInject(): Boolean {
    // TODO Find another way to check to presence of javax.inject.Inject
    return any { annotation -> annotation.shortName.asString() == "Inject" }
}
