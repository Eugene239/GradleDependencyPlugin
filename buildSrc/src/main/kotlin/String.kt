import java.io.File
import java.util.concurrent.TimeUnit

/**
 * This method will be used to execute a command and get a String
 * based on the result, for now, this is used to get the hash
 * of the commit to append into the snapshot name in the next format
 * major.minor.patch-hash-SNAPSHOT
 *
 * ie: 1.1.1-abc123-SNAPSHOT
 *
 * Reference: https://stackoverflow.com/a/52441962/2453030
 */
fun String.runCommand(
        workingDir: File = File("."),
        timeoutAmount: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String = try {
    ProcessBuilder(split("\\s".toRegex()))
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start().apply { waitFor(timeoutAmount, timeoutUnit) }
            .inputStream.bufferedReader().readText()
} catch (e: java.io.IOException) {
    e.printStackTrace()
    "null"
}
