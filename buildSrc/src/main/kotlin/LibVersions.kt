import org.gradle.api.Project

object LibVersions {
    private fun getHash(): String {
        val command = "git rev-parse --short HEAD"
        return command.runCommand().trim()
    }

    fun getVersionName(major: Int, minor: Int, patch: Int): String {
        return "$major.$minor.$patch"
    }

    fun getLibVersion(project: Project, major: Int, minor: Int, patch: Int): String {
        return if (isRelease(project)) "$major.$minor.$patch" else "$major.$minor.$patch-${getHash()}-SNAPSHOT"
    }

    fun getVersionCode(major: Int, minor: Int, patch: Int): Int {
        return (major * 10000) + (minor * 1000) + (patch * 10)
    }

    private fun isRelease(project: Project): Boolean {
        println("isRelease ${project.properties["IS_RELEASE"]}")
        return project.properties["IS_RELEASE"] == "true"
    }
}
