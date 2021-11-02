package structure

import dto.ConvertibleToDto
import dto.FundamentalDto
import elements.ElementClass
import elements.abstract.Node
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import kotlinx.css.th
import utils.collections.KeySet
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf
import utils.collections.toMutableKeySet
import kotlin.properties.Delegates

class Fundamental(
    nodeClasses: Collection<NodeClass> = emptyList(),
    edgeClasses: Collection<EdgeClass> = emptyList(),
    propertyClasses: Collection<PropertyClass> = emptyList()
) : Structure, ConvertibleToDto<FundamentalDto> {

    val size get() = nodeClasses.size + edgeClasses.size + propertyClasses.size

    var schema: Schema? = null

    private val _nodeClasses: MutableKeySet<String, NodeClass> = nodeClasses.toMutableKeySet()
    val nodeClasses get() = _nodeClasses as KeySet<String, NodeClass>

    private val _edgeClasses: MutableKeySet<String, EdgeClass> = edgeClasses.toMutableKeySet()
    val edgeClasses get() = _edgeClasses as KeySet<String, EdgeClass>

    private val _propertyClasses: MutableKeySet<String, PropertyClass> = propertyClasses.toMutableKeySet()
    val propertyClasses get() = _propertyClasses as KeySet<String, PropertyClass>

    init {
        listOf(_nodeClasses, _edgeClasses, _propertyClasses)
            .asSequence()
            .flatten()
            .forEach { it.fundamental = this }
    }

    fun add(elementClass: ElementClass): ElementClass? = when (elementClass) {
        is NodeClass -> addNodeClass(elementClass)
        is EdgeClass -> addEdgeClass(elementClass)
        is PropertyClass -> addPropertyClass(elementClass)
        else -> null
    }

    fun add(elementClasses: Collection<ElementClass>): Collection<ElementClass> =
        elementClasses.mapNotNull { add(it) }

    fun remove(elementClass: ElementClass): ElementClass? = when (elementClass) {
        is NodeClass -> removeNodeClass(elementClass)
        is EdgeClass -> removeEdgeClass(elementClass)
        is PropertyClass -> removePropertyClass(elementClass)
        else -> null
    }

    fun remove(elementClasses: Collection<ElementClass>): Collection<ElementClass> =
        elementClasses.mapNotNull { remove(it) }

    fun addNodeClass(nodeClass: NodeClass): NodeClass? {
        return nodeClass.takeIf { _nodeClasses.add(it) }?.updateFundamental()
    }

    fun addEdgeClass(edgeClass: EdgeClass): EdgeClass? {
        return edgeClass.takeIf { _edgeClasses.add(it) }?.updateFundamental()
    }

    fun addPropertyClass(propertyClass: PropertyClass): PropertyClass? {
        return propertyClass.takeIf { _propertyClasses.add(it) }?.updateFundamental()
    }

    fun removeNodeClass(nodeClass: NodeClass): NodeClass? {
        return nodeClass.takeIf { _nodeClasses.remove(it) }?.nullifyFundamental()
    }

    fun removeEdgeClass(edgeClass: EdgeClass): EdgeClass? {
        return edgeClass.takeIf { _edgeClasses.remove(it) }?.nullifyFundamental()
    }

    fun removePropertyClass(propertyClass: PropertyClass): PropertyClass? {
        return propertyClass.takeIf { _propertyClasses.remove(it) }?.nullifyFundamental()
    }

    private fun <T : ElementClass> T.updateFundamental() = also { fundamental = this@Fundamental }
    private fun <T : ElementClass> T.nullifyFundamental() = also { fundamental = null }


    override fun convert(): FundamentalDto {
        return FundamentalDto(
            nodeClasses.map { it.convert() }.toSet(),
            edgeClasses.map { it.convert() }.toSet(),
            propertyClasses.map { it.convert() }.toSet()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Fundamental) return false
        return nodeClasses == other.nodeClasses &&
                edgeClasses == other.edgeClasses &&
                propertyClasses == other.propertyClasses
    }

    override fun hashCode(): Int {
        var result = _nodeClasses.hashCode()
        result = 31 * result + _edgeClasses.hashCode()
        result = 31 * result + _propertyClasses.hashCode()
        return result
    }

    override fun toString(): String = listOf(
        "nodeClasses: $nodeClasses",
        "edgeClasses: $edgeClasses",
        "propertyClasses: $propertyClasses"
    ).joinToString("\n")
}