package io.github.eugene239.gradle.plugin.dependency.internal.output


internal interface Output<D, F> {

    fun format(data: D): F
}