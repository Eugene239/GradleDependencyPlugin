package io.github.eugene239.gradle.plugin.dependency.internal.service.simplexml

import io.github.eugene239.gradle.plugin.dependency.internal.service.Properties
import org.simpleframework.xml.convert.Converter
import org.simpleframework.xml.stream.InputNode
import org.simpleframework.xml.stream.OutputNode

internal class PropertiesConverter : Converter<Properties> {

    override fun read(node: InputNode?): Properties {
        val map = mutableMapOf<String, String>()
        node?.let {
            var child = it.next
            while (child != null) {
                map[child.name] = child.value.orEmpty()
                child = it.next
            }
        }
        return Properties(
            entries = map
        )
    }

    override fun write(node: OutputNode?, value: Properties?) = Unit
}