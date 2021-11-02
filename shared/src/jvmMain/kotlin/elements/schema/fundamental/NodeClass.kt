package elements.schema.fundamental

import dto.*
import elements.*
import elements.schema.NodeClassType
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import org.janusgraph.core.VertexLabel
import org.janusgraph.core.schema.JanusGraphManagement
import specifications.Cardinality
import specifications.DataType
import structure.Fundamental
import structure.schemaInfo
import utils.*
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

actual data class NodeClass actual constructor(
    override var label: String,

    @JanusProperty(Level.INTERNAL, "name", saveFlag = false)
    override val id: String,

    @JanusProperty(Level.CUSTOM, TYPE_PROPERTY_NAME)
    actual override val type: NodeClassType
) : ElementClassPropertyHolder, ConvertibleToDto<NodeClassDto> {

    override val properties: MutableKeySet<String, PropertyRelation> = mutableKeySetOf()

    override var fundamental: Fundamental? = null

    actual val hasModelNode: Boolean?
        get() = schemaInfo?.hasModelNode

    actual val modelNode: ModelNode?
        get() = schemaInfo?.modelNode

    //using for simple testing
    @JanusProperty(level = Level.CUSTOM)
    var testProperty: String = ""

    //using for simple testing
    @JanusProperty(level = Level.CUSTOM)
    @PromisedJanusType(DataType.STRING, Cardinality.SINGLE)
    var testPropertySingle: TestEnum? by withDefault<TestEnum?>(null)
        .pack { it?.name }
        .unpack { it?.let { it2 -> TestEnum.valueOf(it2) } }

    //using for simple testing
    @JanusProperty(level = Level.CUSTOM)
    @PromisedJanusType(DataType.STRING, Cardinality.SINGLE)
    var testPropertySingle2: TestEnum by Identifiable(
        TestEnum::name,
        { TestEnum.valueOf(it) },
        value = TestEnum.VALUE_ONE
    )

    //using for simple testing
    @JanusProperty(level = Level.CUSTOM)
    @PromisedJanusType(DataType.STRING, Cardinality.LIST)
    var testPropertyList: List<TestEnum>? by Identifiable(
        { it?.map(TestEnum::name) },
        { it?.map(TestEnum::valueOf) },
        value = listOf(TestEnum.VALUE_ONE, TestEnum.VALUE_TWO)
    )

    override fun convert(): NodeClassDto = transform {
        NodeClassDto::properties.exact from properties.map { it.convert() }.toSet()
    }

    companion object : ElementClassStatic<VertexLabel> {
        override fun getAllElements(mgmt: JanusGraphManagement): Iterable<VertexLabel> = mgmt.vertexLabels
        override fun getById(mgmt: JanusGraphManagement, id: String): VertexLabel? = mgmt.getVertexLabel(id)
    }
}

//using for simple testing
enum class TestEnum {
    VALUE_ONE,
    VALUE_TWO
}