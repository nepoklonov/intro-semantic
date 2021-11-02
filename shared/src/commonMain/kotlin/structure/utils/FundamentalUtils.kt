package structure.utils

import elements.inheritanceEdgeClass
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import structure.Fundamental

fun Fundamental.withNodeClasses(vararg nodeClasses: NodeClass) = apply {
    nodeClasses.forEach { addNodeClass(it) }
}

fun Fundamental.withEdgeClasses(vararg edgeClasses: EdgeClass) = apply {
    edgeClasses.forEach { addEdgeClass(it) }
}

fun Fundamental.withPropertyClasses(vararg propertyClasses: PropertyClass) = apply {
    propertyClasses.forEach { addPropertyClass(it) }
}

fun Fundamental.withInheritance() = apply {
    if (inheritanceEdgeClass !in edgeClasses) addEdgeClass(inheritanceEdgeClass)
}