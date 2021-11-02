package elements

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties

actual fun setPropertiesFromMap(map: Map<String, Any?>, element: SchemaElement): SchemaElement {
    val kProperties = element::class.declaredMemberProperties.associateBy { it.name }
    map.forEach { (key, value) ->
        kProperties[key]?.let { kProperty1 ->
            if (kProperty1 is KMutableProperty<*>) kProperty1.setter.call(element, value)
        }
    }
    return element
}

actual fun getPropertiesFromMap(map: Map<String, Any?>, element: SchemaElement): Map<String, Any?> {
    val kProperties = element::class.declaredMemberProperties.associateBy { it.name }
    return map.map { (key, _) ->
        key to kProperties[key]?.getter?.call(element)
    }.toMap()
}