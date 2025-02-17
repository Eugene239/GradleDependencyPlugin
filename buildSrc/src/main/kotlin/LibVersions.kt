import org.gradle.api.Project

object LibVersions {
    private fun getHash(): String {
        val command = "git rev-parse --short HEAD"
        return command.runCommand().trim()
    }

    fun getLibVersion(project: Project, major: Int, minor: Int, patch: Int): String {
        return if (isRelease(project)) "$major.$minor.$patch" else "$major.$minor.$patch-${getHash()}-SNAPSHOT"
    }

    private fun isRelease(project: Project): Boolean {
        return project.properties["IS_RELEASE"] == "true"
    }
}
