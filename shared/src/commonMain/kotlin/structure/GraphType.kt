package structure

import elements.abstract.Edge
import elements.abstract.Node
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode

//probably, AbstractionLevel is better
sealed class GraphType<N : Node<N, E>, E : Edge<N, E>> {
    object Model : GraphType<ModelNode, EdgeRelation>()
    object Data : GraphType<NodeInstance, EdgeInstance>()
}