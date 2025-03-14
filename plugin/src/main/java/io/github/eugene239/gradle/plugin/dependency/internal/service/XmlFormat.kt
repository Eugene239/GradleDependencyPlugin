package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.service.simplexml.PropertiesConverter
import org.simpleframework.xml.convert.Registry
import org.simpleframework.xml.convert.RegistryStrategy
import org.simpleframework.xml.core.Persister

internal object XmlFormat {

    val serializer = Persister(
        RegistryStrategy(
            Registry()
                .bind(Properties::class.java, PropertiesConverter::class.java)
        )
    )
}