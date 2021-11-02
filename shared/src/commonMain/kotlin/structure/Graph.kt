package structure

import elements.GraphElement
import elements.abstract.Edge
import elements.abstract.Node
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.generateEdgeRelationKey
import elements.inheritanceEdgeClass
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import utils.collections.KeySet
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf
import kotlin.reflect.KClass

//TODO maybe return collections from all functions
abstract class Graph<N : Node<N, E>, E : Edge<N, E>>(
    nodes: Collection<N>,
    edges: Collection<E>,
) : Structure, Graphable<N, E>, GraphElementSet<N, E> {

    abstract val graphType: GraphType<N, E>

    open var schema: Schema? = null

    val size get() = nodes.size + edges.size

    val matrix: GraphMatrix<N, E> = GraphMatrix()
    private val _nodes: MutableKeySet<String, N> = mutableKeySetOf()
    private val _edges: MutableKeySet<String, E> = mutableKeySetOf()

    init {
        nodes.map { addNode(it) }
        edges.map { addEdge(it) }
    }

    override val nodes: KeySet<String, N> get() = _nodes
    override val edges: KeySet<String, E> get() = _edges

    override fun addNode(node: N): N? {
        return if (!_nodes.contains(node)) {
            _nodes += node
            node.updateGraph()
            matrix.nodeInfo[node.key] = NodeInfo()
            node
        } else null
    }

    override fun addEdge(edge: E): E? {
        val source = edge.source
        val target = edge.target
        return if (edge !in _edges && source in _nodes && target in _nodes) {
            _edges += edge
            edge.updateGraph()
            matrix.edgeInfo[edge.key] = EdgeInfo()
            val sourceMatrix = matrix.nodeInfo[source.key] ?: throw Exception()
            val targetMatrix = matrix.nodeInfo[target.key] ?: throw Exception()
            sourceMatrix.connectedEdges.add(edge)
            targetMatrix.connectedEdges.add(edge)
            if (edge.elementClass == inheritanceEdgeClass) {
                sourceMatrix.parent = target
                targetMatrix.children.add(source)
            }
            edge
        } else null
    }

    override fun removeNode(node: N): GraphElementSet<N, E>? {
        return if (node in _nodes) {
            val connectedEdges = node.connectedEdges?.toList() ?: emptyList()
            val removedEdges = connectedEdges.map {
                removeEdge(it) ?: throw IllegalStateException()
            }
            node.nullifyGraph()
            matrix.nodeInfo.remove(node.key)
            _nodes.remove(node)
            val removedNodes = listOf(node)
            return graphElementSetOf(removedNodes, removedEdges)
        } else null
    }

    override fun removeEdge(edge: E): E? {
        return if (edge in _edges) {
            edge.nullifyGraph()
            matrix.edgeInfo.remove(edge.key)
            val source = edge.source
            val target = edge.target
            val sourceMatrix = matrix.nodeInfo[source.key] ?: throw  Exception()
            val targetMatrix = matrix.nodeInfo[target.key] ?: throw  Exception()
            sourceMatrix.connectedEdges.remove(edge)
            targetMatrix.connectedEdges.remove(edge)
            if (edge.elementClass == inheritanceEdgeClass) {
                sourceMatrix.parent = null
                targetMatrix.children.remove(source)
            }
            _edges.remove(edge)
            return edge
        } else null
    }

    override fun reverseEdge(edge: E): E? {
        val reversedEdge = edge.createReversed()
        removeEdge(edge)
        addEdge(reversedEdge)
        return reversedEdge
    }

    fun rollbackNodeRemoval (graphElementSet: GraphElementSet<N, E>){
        addNodes(graphElementSet.nodes)
        addEdges(graphElementSet.edges)
    }

    private fun GraphElement<N, E>.updateGraph() = also { it.graph = this@Graph }
    private fun GraphElement<N, E>.nullifyGraph() = also { it.graph = null }
}

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.graphInfo get() = graph?.matrix?.nodeInfo?.get(key)
val <N : Node<N, E>, E : Edge<N, E>> Edge<N, E>.graphInfo get() = graph?.matrix?.edgeInfo?.get(key)