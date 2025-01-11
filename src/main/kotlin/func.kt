package symbolicExecution

import ast.ASTBuilder
import generated.mygrammarLexer
import generated.mygrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

import ast.*

fun evalExpression(memory: Memory, expr: Expression): String  {
    if(expr is IntConstant)
        return expr.value.toString()
    else if(expr is BoolConstant)
        return expr.value.toString()
    else if(expr is VarRef)
        return memory[expr.identifier] ?: error("Variable '${expr.identifier}' not initialized")
    else if(expr is BinOp) {
        val left = evalExpression(memory, expr.lhs)
        val right = evalExpression(memory, expr.rhs)
        return "$left ${expr.kind} $right"
    }
    else if(expr is UnOp) {
        val inner = evalExpression(memory, expr.subExpr)
        return "${expr.kind}($inner)"
    }
    else throw Exception("Unknown expr")
}

fun formatState(pState: ProgramState): String{
    val memory = pState.memory.map { elem ->  elem.key + " = " + elem.value}
    return """
{
${memory.joinToString("\n")}
pc: ${pState.pathCondition.joinToString(" && ")}
result: ${pState.result}
}
    """.trimIndent()
}

fun processFile(inputFile: File): List<String> {
    val input = CharStreams.fromStream(FileInputStream(inputFile))
    val myLexer = mygrammarLexer(input)
    val commonToken = CommonTokenStream(myLexer)
    val myParser = mygrammarParser(commonToken)

    val tree = myParser.function()

    val builder = ASTBuilder.create().apply {
        ParseTreeWalker.DEFAULT.walk(this, tree)
    }

    val function = builder.getFunction()

    return executeFunction(function).map { formatState(it) }
}

fun writeResults(outputFile: File, results: List<String>) {
    PrintWriter(outputFile).use { writer ->
        results.forEach(writer::println)
    }
}