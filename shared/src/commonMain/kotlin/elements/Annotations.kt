package elements

import specifications.Cardinality
import specifications.DataType

//TODO перенести в jvmMain

@Target(AnnotationTarget.PROPERTY)
annotation class JanusProperty(
    val level: Level,
    val name: String = "",
    val saveFlag: Boolean = true,
    val getFlag: Boolean = true,
)

@Target(AnnotationTarget.PROPERTY)
annotation class PromisedJanusType(
    val dataType: DataType,
    val cardinality: Cardinality
)

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class JanusConstructor

@Target(AnnotationTarget.PROPERTY)
annotation class IrregularDataProperty(
    val name: String = "",
    val saveFlag: Boolean = true,
    val getFlag: Boolean = true,
)

enum class Level {
    INTERNAL,
    CUSTOM
}