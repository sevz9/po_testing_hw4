package symbolicExecution

import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.size < 2) {
        System.err.println("2 files required: input and out")
        return
    }

    val inPath = Paths.get(args[0])
    val outPath = Paths.get(args[1])

    val inputFile = inPath.toFile()
    val outputFile = outPath.toFile()

    if (!inputFile.exists()) {
        System.err.println("File not found: $inputFile")
        return
    }

    writeResults(outputFile, processFile(inputFile))
}

