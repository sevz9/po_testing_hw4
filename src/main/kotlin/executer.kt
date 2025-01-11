package symbolicExecution

import ast.Function
import ast.Assignment
import ast.IfStmt
import ast.ReturnStmt
import ast.Statement

typealias Memory = MutableMap<String, String>
typealias PathCondition = MutableList<String>

data class ProgramState(
    val memory: Memory,
    val pathCondition: PathCondition,
    val stack: MutableList<Statement>,
    var result: String? = null
)

fun execute(initialState: ProgramState): List<ProgramState> {
    val completedStates = mutableListOf<ProgramState>()
    val executionQueue = mutableListOf(initialState)
    while (executionQueue.isNotEmpty()) {
        val currentState = executionQueue.removeFirst()

        if (currentState.stack.isEmpty()) {
            completedStates.add(currentState)
            continue
        }

        val stmt = currentState.stack.removeFirst()

        if(stmt is Assignment){
            currentState.memory[stmt.name] = evalExpression(currentState.memory, stmt.value)
            executionQueue.add(currentState)
        }
        else if(stmt is IfStmt){
            val condition = evalExpression(currentState.memory, stmt.condition)
            val trueBranch = ProgramState(
                memory = currentState.memory.toMutableMap(),
                pathCondition = currentState.pathCondition.toMutableList().apply { add(condition) },
                stack = (stmt.thenBlock.toMutableList() + currentState.stack).toMutableList(),
                result = currentState.result
            )

            val falseBranch = ProgramState(
                memory = currentState.memory.toMutableMap(),
                pathCondition = currentState.pathCondition.toMutableList().apply { add("!($condition)") },
                stack = (stmt.elseBlock.toMutableList() + currentState.stack).toMutableList(),
                result = currentState.result
            )

            executionQueue.add(trueBranch)
            executionQueue.add(falseBranch)
        }
        else if(stmt is ReturnStmt) {
            currentState.result = evalExpression(currentState.memory, stmt.returnExpr)
            executionQueue.add(currentState)
        }
        else
            throw Exception("Unknown stmt")

    }
    return completedStates
}

fun executeFunction(function: Function): List<ProgramState> {
    val program = function.body + ReturnStmt(function.returnValue!!)
    val initialState = ProgramState(
        memory = mutableMapOf(),
        pathCondition = mutableListOf(),
        stack = program.toMutableList()
    )
    function.parameters.forEach { param ->
        initialState.memory[param.name] = "'${param.name}'"
    }

    return execute(initialState)
}
