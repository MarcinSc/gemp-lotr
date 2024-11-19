package com.gempukku.context.processor.inject

import com.gempukku.context.GempukkuContext
import com.gempukku.context.processor.SystemProcessor
import com.gempukku.context.processor.inject.decorator.SystemDecorator
import com.gempukku.context.processor.inject.property.PropertyResolver
import com.gempukku.context.resource.FileResource
import com.gempukku.context.resource.FileResourceResolver
import com.gempukku.context.resource.createDefaultFileResourceResolver
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.util.logging.Logger

private val log: Logger = Logger.getLogger(AnnotationSystemInjector::class.java.name)

class AnnotationSystemInjector(
    private val propertyResolver: PropertyResolver? = null,
    private val systemDecorator: SystemDecorator? = null,
    private val fileResourceResolver: FileResourceResolver = createDefaultFileResourceResolver(),
) : SystemProcessor {
    private val usedProperties = mutableSetOf<String>()

    override fun processSystems(context: GempukkuContext, systems: Collection<Any>) {
        systems.forEach { system ->
            system.javaClass.declaredFields.forEach { field ->
                if (field.isAnnotationPresent(Inject::class.java)) {
                    processInject(field, context, system)
                }
                if (field.isAnnotationPresent(InjectList::class.java)) {
                    processInjectList(field, context, system)
                }
                if (field.isAnnotationPresent(InjectValue::class.java)) {
                    processInjectProperty(field, system)
                }
            }
        }
        propertyResolver?.getAllPropertyNames()?.filter { !usedProperties.contains(it) }?.forEach { propertyName ->
            log.warning("Property not used in context: $propertyName")
        }
        usedProperties.clear()
    }

    private fun processInjectProperty(field: Field, system: Any) {
        if (propertyResolver == null) {
            throw InjectionException("Unable to inject property, property resolver is missing")
        }

        val injectAnnotation = field.getAnnotation(InjectValue::class.java)
        val fieldType = field.type
        when (fieldType) {
            String::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it)
                }
            }

            Int::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it.toInt())
                }
            }

            Long::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it.toLong())
                }
            }

            Float::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it.toFloat())
                }
            }

            Double::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it.toDouble())
                }
            }

            Boolean::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, it.toBoolean())
                }
            }

            FileResource::class.java -> {
                resolveProperty(injectAnnotation.value)?.let {
                    field.trySetAccessible()
                    field.set(system, fileResourceResolver.resolveFileResource(it))
                }
            }
        }
    }

    private fun resolveProperty(propertyName: String): String? {
        val propertyValue = propertyResolver!!.resolveProperty(propertyName)
        if (propertyValue == null) {
            log.warning("Unable to resolve property: $propertyName")
        } else {
            usedProperties.add(propertyName)
        }
        return propertyValue
    }

    private fun processInjectList(field: Field, context: GempukkuContext, system: Any) {
        val injectAnnotation = field.getAnnotation(InjectList::class.java)
        val fieldType = field.type
        if (List::class.java.isAssignableFrom(fieldType)) {
            val typeParameter = (field.genericType as ParameterizedType).actualTypeArguments[0]
            val typeClass =
                when (typeParameter) {
                    is WildcardType -> typeParameter.upperBounds[0]
                    else -> typeParameter
                } as Class<Any>
            val priorityPrefix =
                injectAnnotation.priorityPrefix.takeIf { it.isNotBlank() } ?: typeClass
                    .getAnnotation(
                        DefaultPriorityPrefix::class.java,
                    )?.value
            val resolvedSystems =
                findValues(context, typeClass, injectAnnotation, priorityPrefix)
                    .sortedBy { -it.second }
                    .map { it.first }
                    .map { system ->
                        decorateIfNeeded(system, typeClass)
                    }
            field.trySetAccessible()
            field.set(system, resolvedSystems)
        } else {
            throw InjectionException(
                "Unable to inject systems annotated with @InjectList into ${system.javaClass.name}::${field.name}, is not a List",
            )
        }
    }

    private fun processInject(field: Field, context: GempukkuContext, system: Any) {
        val injectAnnotation = field.getAnnotation(Inject::class.java)
        val fieldType = field.type as Class<Any>

        val resolvedValues = findValues(context, fieldType, injectAnnotation)
        when (resolvedValues.size) {
            1 -> {
                field.trySetAccessible()
                field.set(system, decorateIfNeeded(resolvedValues.first(), fieldType))
            }

            0 -> {
                if (!injectAnnotation.allowsNull) {
                    throw InjectionException(
                        "Unable to inject system annotated with @Inject into ${system.javaClass.name}::${field.name}, " +
                            "system not found and not allowed to be null",
                    )
                }
            }

            else -> {
                throw InjectionException(
                    "Unable to inject system annotated with @Inject into ${system.javaClass.name}::${field.name}, " +
                        "multiple systems matching criteria found",
                )
            }
        }
    }

    private fun decorateIfNeeded(system: Any, typeClass: Class<Any>) = systemDecorator?.let { systemDecorator.decorate(system, typeClass) } ?: system

    private fun findValues(context: GempukkuContext, clazz: Class<out Any>, injectAnnotation: Inject): List<Any> {
        val systems = context.getSystems(clazz)
        return systems.ifEmpty {
            if (injectAnnotation.firstNotNullFromAncestors && context.parent != null) {
                findValues(context.parent!!, clazz, injectAnnotation)
            } else {
                emptyList()
            }
        }
    }

    private fun findValues(
        context: GempukkuContext,
        clazz: Class<out Any>,
        injectAnnotation: InjectList,
        priorityPrefix: String?,
    ): List<Pair<Any, Int>> {
        val systems =
            context.getSystems(clazz).map { system ->
                val priority =
                    if (priorityPrefix != null) {
                        val annotation = system.javaClass.getAnnotation(PriorityPostfix::class.java)
                        val postfix = annotation?.value?.takeIf { it.isNotBlank() }
                        if (postfix != null) {
                            getPriority("$priorityPrefix.$postfix")
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                system to priority
            }

        return if (injectAnnotation.selectFromAncestors && context.parent != null) {
            systems + findValues(context.parent!!, clazz, injectAnnotation, priorityPrefix)
        } else {
            systems
        }
    }

    private fun getPriority(key: String): Int =
        propertyResolver?.let {
            resolveProperty("priority.$key")?.toInt() ?: 0
        } ?: 0
}
