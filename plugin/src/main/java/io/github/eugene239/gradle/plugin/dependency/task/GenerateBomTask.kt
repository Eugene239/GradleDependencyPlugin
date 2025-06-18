package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.output.bom.BomOutput
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GenerateBomUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GenerateBomUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar

internal abstract class GenerateBomTask : BaseTask() {

    @Input
    @Option(option = "bom-library-name", description = "Bom library name to use in Pom")
    var bomLibName: String = ""

    override suspend fun exec() {
        val split = bomLibName.split(":")

        val libKey = if (bomLibName.isNotBlank()) {
            LibKey(
                split[0],
                split[1],
                split[2]
            )
        } else {
            LibKey(
                group = "${project.group}",
                module = "BOM",
                version = GregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
            )
        }

        val useCase = GenerateBomUseCase()

        val data = useCase.execute(
            GenerateBomUseCaseParams(
                configurations = project.filteredConfigurations(),
                library = libKey
            )
        )

        val output = BomOutput()
        val result = output.format(data)

        logger.lifecycle("Report in file://${result.path}")
    }
}