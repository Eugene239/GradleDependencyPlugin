package io.github.eugene239.gradle.plugin.dependency.internal.service

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import nl.adaptivity.xmlutil.serialization.defaultSharedFormatCache

internal object XmlFormat {

    @OptIn(ExperimentalXmlUtilApi::class)
    val format = XML {
//        repairNamespaces = true
        policy = DefaultXmlSerializationPolicy(formatCache = defaultSharedFormatCache()) {
            unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
        }
        isCachingEnabled = false
  //      autoPolymorphic = true
    }
}