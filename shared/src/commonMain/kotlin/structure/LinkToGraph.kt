package structure

import elements.abstract.Edge
import elements.abstract.Node
import elements.schema.fundamental.NodeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import kotlin.reflect.KProperty

//interface G {
//    fun addA(a: A)
//    fun containsA(a: A): Boolean
//}

object GraphManagementSystem {
    private val modelGraphs: List<ModelGraph> = TODO()
    fun getGraphByNode(node: ModelNode): Graph<ModelNode, EdgeRelation>? = modelGraphs.firstOrNull {
        it.nodes.contains(node)
    }
}

class LinkToGraph<N: Node<N, E>, E: Edge<N, E>>(val source: MM) {
    private var _graph: Graph<N, E>? = null
    operator fun getValue(modelNode: MM, property: KProperty<*>): Graph<N, E>? {
        return _graph
    }

    operator fun setValue(modelNode: MM, property: KProperty<*>, graph: Graph<N, E>?) {
        if (graph == null) {
            check(_graph?.nodes?.contains(modelNode as N) != true)
            _graph = null
        } else {
            check(_graph?.nodes?.contains(modelNode as N) != true)
            graph.nodes.contains(modelNode as N)
        }
    }
}

val Graph<ModelNode, EdgeRelation>.host: Fundamental? get() = TODO()

class ProtoEdgeModelNodeToNodeClass(val source: MM) {
    private var _target: NodeClass? = null

    operator fun getValue(MM: MM, property: KProperty<*>): NodeClass? = _target

    operator fun setValue(MM: MM, property: KProperty<*>, nodeClass: NodeClass?) {
        MM.graph?.let { graph ->
            graph.host?.let {
                    fundamental -> check(fundamental.nodeClasses.contains(nodeClass))
            }
        }
        _target = nodeClass
    }
}

class MM {
    var graph: Graph<ModelNode, EdgeRelation>? by LinkToGraph(this)
    var nodeClass: NodeClass? by ProtoEdgeModelNodeToNodeClass(this)
}


fun main() {
//    MN().graph = "*clown*" as Graph<ModelNode, EdgeRelation>
}

//fun GraphDecorator<ModelNode, EdgeRelation>.addMN(mm: MM) {
//    graph.addNode(mm as ModelNode)
//}

//    val graph get() = GraphManagementSystem.getGraphByNode(this as ModelNode)
//    private var _graph : G? = null
//    var s: G? = _graph
//        get() = _graph
//        set(value) {
//            if (_graph?.containsA(this) != true) throw IllegalArgumentException()
//            field = value
//        }
