package elements

import elements.abstract.Edge
import elements.abstract.Node
import elements.abstract.Property
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.ElementClassType
import elements.schema.fundamental.EdgeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import structure.Fundamental
import structure.Graph
import utils.collections.KeyElement
import utils.collections.MutableKeySet

interface OverallElement : KeyElement<String> {
    val id: String
    val label: String
}

interface SchemaElement : OverallElement

interface Element<N : Node<N, E>, E : Edge<N, E>> : OverallElement {
    val elementClass: ElementClass?
}

interface GraphElement<N : Node<N, E>, E : Edge<N, E>> : Element<N, E> {
    var graph: Graph<N, E>?
    fun relatedEntities(): Pair<Set<N>, Set<E>>?
    val properties: MutableKeySet<String, out Property<N, E>>
    val tangibility: Tangibility
}

interface ModelElement : SchemaElement, Element<ModelNode, EdgeRelation>

interface ModelGraphElement : ModelElement, GraphElement<ModelNode, EdgeRelation> {
    override val properties: MutableKeySet<String, PropertyRelation>
}

interface DataElement : Element<NodeInstance, EdgeInstance> {
    val modelElement: ModelElement?
    override val key: String get() = id
}

interface DataGraphElement : DataElement, GraphElement<NodeInstance, EdgeInstance> {
    override val properties: MutableKeySet<String, PropertyInstance>
}

interface ElementClass : SchemaElement {
    val type: ElementClassType<*>
    var fundamental: Fundamental?
    override val key: String get() = label
}

interface ElementClassPropertyHolder : ElementClass {
    val properties: MutableKeySet<String, PropertyRelation>
}

interface PlatformNode<N : Node<N, E>, E : Edge<N, E>>

interface PlatformEdge<N : Node<N, E>, E : Edge<N, E>>

expect object Elements {
    val inheritanceEdgeClass: EdgeClass
}