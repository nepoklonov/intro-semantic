package elements.abstract

import elements.Element
import elements.schema.fundamental.PropertyClass

interface Property<N: Node<N, E>, E: Edge<N, E>> : Element<N, E> {
    override val elementClass: PropertyClass
}