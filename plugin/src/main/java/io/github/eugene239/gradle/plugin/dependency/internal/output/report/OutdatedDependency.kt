package io.github.eugene239.gradle.plugin.dependency.internal.output.report

internal data class OutdatedDependency(
    val name: String,
    val currentVersion: String,
    val latestVersion: String,
    val status: DependencyStatus = DependencyStatus.determine(currentVersion, latestVersion),
)

internal enum class DependencyStatus(private val text: String) {
    OK("✅"), WARN("⚠\uFE0F"), FAIL("❗");

    override fun toString(): String {
        return text
    }

    companion object {

        fun determine(version: String, latestVersion: String): DependencyStatus {
            if (version == latestVersion) return OK
            val currentMajor = version.split(".").first().toIntOrNull() ?: 0
            val latestMajor = latestVersion.split(".").first().toIntOrNull() ?: 0
            return if (currentMajor < latestMajor) {
                FAIL
            } else {
                WARN
            }
        }
    }
}