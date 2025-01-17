package io.gitlab.arturbosch.detekt.core

import io.github.detekt.test.utils.resourceAsPath
import io.gitlab.arturbosch.detekt.api.FileProcessListener
import io.gitlab.arturbosch.detekt.test.yamlConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import java.lang.reflect.Modifier

/**
 * This tests the existence of all metric processors in the META-INF config file in the core package
 */
class FileProcessorLocatorSpec {

    @Nested
    inner class `file processor locator` {

        val path = resourceAsPath("")

        @Test
        fun `contains all processors`() {
            val processors = createProcessingSettings(path).use { FileProcessorLocator(it).load() }
            val processorClasses = getProcessorClasses()

            assertThat(processorClasses).isNotEmpty
            processorClasses
                .filter { clazz -> processors.none { clazz == it.javaClass } }
                .forEach { fail("$it processor is not loaded by the FileProcessorLocator") }
        }

        @Test
        fun `has disabled processors`() {
            val config = yamlConfig("configs/disabled-processors.yml")
            val processors = createProcessingSettings(path, config).use { FileProcessorLocator(it).load() }
            assertThat(processors).isEmpty()
        }
    }
}

private fun getProcessorClasses(): List<Class<out FileProcessListener>> {
    return Reflections("io.github.detekt.metrics.processors")
        .getSubTypesOf(FileProcessListener::class.java)
        .filter { !Modifier.isAbstract(it.modifiers) }
}
