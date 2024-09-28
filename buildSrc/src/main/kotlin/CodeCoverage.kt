import org.gradle.api.Project

val Project.isCodeCoverageEnabled
    get() = properties.containsKey("coverageEnabled")

fun Project.applyCodeCoverageIfEnabled() {
    if (isCodeCoverageEnabled) {
        apply { from(rootProject.file("./buildSrc/jacoco.gradle")) }
    }
}
