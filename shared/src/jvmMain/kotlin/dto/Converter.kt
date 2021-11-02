package dto

import elements.JanusConstructor
import utils.ExactKProperty
import utils.Identifiable
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

fun <T : KCallable<*>> Iterable<T>.findByName(name: String?) = find { it.name == name }

@Suppress("UNCHECKED_CAST")
fun <T : Any> createObjectFromParamsMap(paramsMap: Map<String, Any?>, kClass: KClass<T>, fromJanus: Boolean = false): T {
    val mutableParamsMap = paramsMap.toMutableMap()
    val constructor = kClass.constructors
        .takeIf { fromJanus }
        ?.firstOrNull { it.findAnnotation<JanusConstructor>() != null }
        ?: kClass.primaryConstructor!!

    val constructorMap = constructor.parameters.associateWith { kParameter ->
        mutableParamsMap[kParameter.name]?.let { propValue ->
            convertToParameterType(kParameter, propValue)
        }.also { mutableParamsMap.remove(kParameter.name) }
    }.filterValues { it != null }

    val element = constructor.callBy(constructorMap)

    val declaredMemberProperties = kClass.declaredMemberProperties.associateBy { it.name }

    mutableParamsMap.forEach { (key, value) ->
        declaredMemberProperties[key]?.let { property ->
            if (property is KMutableProperty<*>) {
                var settingValue = value
                property.isAccessible = true
                if (property.getDelegate(element) != null) {
                    settingValue = (property.getDelegate(element) as Identifiable<Any?, Any?>).unpack(value)
                }
                property.setter.call(element, settingValue)
            }
        }
    }

    return element
}

//TODO разбить на две функции
@Suppress("UNCHECKED_CAST")
inline fun <reified F : Convertible, reified T : Convertible> F.transform(block: Converter<T>.() -> Unit = {}): T {
    val toKClass = T::class
    val toClassPropsMap = toKClass.declaredMemberProperties.associateBy { it.name }
    val defaultParams = (this::class as KClass<F>).memberProperties.mapNotNull {
        toClassPropsMap[it.name]?.let { key -> key to it.get(this) }
    }.toMap()
    val additionalParams = Converter(toKClass).apply(block).map
    val finalParams =
        (defaultParams + additionalParams).filter { it.value != null || it.key.returnType.isMarkedNullable }
            .mapKeys { it.key.name }

    return createObjectFromParamsMap(finalParams, toKClass)
}

data class Converter<C : Convertible>(
    val convertibleClass: KClass<C>,
    val map: MutableMap<KProperty1<C, *>, Any?> = mutableMapOf(),
) {
    infix fun <T> ExactKProperty<T>.from(value: T?) {
        map[convertibleClass.memberProperties.findByName(kProperty.name)!!] = value
    }
}

private fun convertToEnumValue(kClass: KClass<*>, value: Any): Any? {
    return kClass.java.enumConstants.singleOrNull { (it as Enum<*>).toString() == value.toString() }
}

fun convertToParameterType(kParameter: KParameter, value: Any): Any? {
    val kClass = kParameter.type.jvmErasure
    return when {
        kClass.isSubclassOf(Enum::class) -> convertToEnumValue(kClass, value)
        else -> value
    }
}