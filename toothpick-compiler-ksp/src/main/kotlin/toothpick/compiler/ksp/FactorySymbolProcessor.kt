package toothpick.compiler.ksp

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate

class FactorySymbolProcessor(
    private val options: Map<String, String>,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val visitor = ClassVisitor(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val injectClasses = resolver.getSymbolsWithAnnotation(INJECT_ANNOTATION_CLASS_NAME)
        val unableToProcess = injectClasses.filterNot { it.validate() }
            .onEach { logger.info("#OPZ#invalid# ${it.javaClass.name}") }

        injectClasses.filter { it is KSFunctionDeclaration && it.validate() && it.isConstructor() }
            .filterIsInstance(KSFunctionDeclaration::class.java)
            .onEach {
                logger.info("#OPZ#OK-1# ${it.javaClass.name}")
                logger.info("#OPZ#OK-2# ${it.parentDeclaration as? KSClassDeclaration}")
            }
//            .mapNotNull { it.parentDeclaration as? KSClassDeclaration }
            .forEach { it.accept(visitor, Unit) }

        return unableToProcess.toList()
    }
}
