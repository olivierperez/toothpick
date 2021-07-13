package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import toothpick.compiler.ksp.INJECT_ANNOTATION_CLASS_NAME
import toothpick.compiler.ksp.factory.generators.FactoryCodeGenerator

class FactorySymbolProcessor(
    private val options: Map<String, String>,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val visitor = ClassVisitor(logger)
        val factoryCodeGenerator = FactoryCodeGenerator(codeGenerator)

        val injectedSymbols = resolver.getSymbolsWithAnnotation(INJECT_ANNOTATION_CLASS_NAME)

        val unableToProcess = injectedSymbols.filterNot { it.validate() }
            .onEach { logger.info("#OPZ#invalid# ${it.javaClass.name}") }

        injectedSymbols.filter { it.validate() }
            .filterIsInstance(KSFunctionDeclaration::class.java)
            .forEach { it.accept(visitor, Unit) }

        visitor.targets.forEach {
            factoryCodeGenerator.generate(it)
        }

        return unableToProcess.toList()
    }
}
