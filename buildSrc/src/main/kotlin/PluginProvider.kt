import org.gradle.api.Project
import java.io.File

object PluginProvider {

    fun getPluginClasspath(project: Project): String? {
        val repo = File("${project.rootDir}/localMavenRepo/io/github/eugene239/gradle-plugin-dependency")
        val dir = repo.listFiles()?.filter { it.isDirectory }?.maxBy { it.lastModified() }
        if (dir != null) {
            return "io.github.eugene239:gradle-plugin-dependency:${dir.name}"
        }
        return null
    }
}