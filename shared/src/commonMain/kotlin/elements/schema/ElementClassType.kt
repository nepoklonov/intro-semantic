package elements.schema

import elements.ElementClass
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass

interface ElementClassType<T : ElementClass>

enum class NodeClassType : ElementClassType<NodeClass> {
    SEMANTIC_ELEMENT
}

enum class PropertyClassType : ElementClassType<PropertyClass> {
    SEMANTIC_ELEMENT,
    TYPE_PROPERTY_CLASS,
    META_PROPERTY_CLASS
}

enum class EdgeClassType : ElementClassType<EdgeClass> {
    SEMANTIC_ELEMENT,
    INHERITANCE_EDGE_CLASS
}