package dto

import elements.ElementClassPropertyHolder
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.inheritanceEdgeClass
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import elements.schema.model.allProperties
import elements.utils.copy
import specifications.DataType
import structure.*
import utils.collections.MutableKeySet
import utils.collections.toMutableKeySet
import utils.safeLet

class SchemaDtoConverter {
    fun SchemaDto.convert(): Schema {
        val fundamental = FundamentalDtoConverter().run { fundamentalDto.convert() }
        val modelGraph = ModelGraphDtoConverter(fundamental).run { modelGraphDto.convert() }
        return Schema(fundamental, modelGraph)
    }
}

class FundamentalDtoConverter {
    fun FundamentalDto.convert(): Fundamental {
        val propertyClasses = propertyClasses.map { it.convert() }.toMutableKeySet()
        val tmpFundamental = Fundamental(propertyClasses = propertyClasses)
        val nodeClasses = nodeClasses.map { it.convert(tmpFundamental) }.toMutableKeySet()
        val edgeClasses = edgeClasses.map { it.convert(tmpFundamental) }.toMutableKeySet()
        return Fundamental(nodeClasses, edgeClasses, propertyClasses)
    }

    fun NodeClassDto.convert(fundamental: Fundamental): NodeClass = NodeClass(
        label = label,
        type = type,
    ).also {
        val convertedProperties = makeProperties(properties, it, fundamental)
        it.properties.addAll(convertedProperties)
    }

    fun EdgeClassDto.convert(fundamental: Fundamental): EdgeClass = if (label == inheritanceEdgeClass.label) {
        inheritanceEdgeClass.copy(label = label, type = type)
    } else {
        EdgeClass(label = label, type = type)
    }.also {
        val convertedProperties = makeProperties(properties, it, fundamental)
        it.properties.addAll(convertedProperties)
    }

    private fun makeProperties(
        properties: Set<PropertyRelationDto>,
        holder: ElementClassPropertyHolder,
        fundamental: Fundamental
    ): Collection<PropertyRelation> = properties.mapNotNull { it.convert(holder, fundamental) }

    fun PropertyClassDto.convert(): PropertyClass = PropertyClass(
        label = label,
        dataType = dataType,
        cardinality = cardinality,
        type = type
    )

    fun PropertyRelationDto.convert(holder: ElementClassPropertyHolder, fundamental: Fundamental): PropertyRelation? =
        fundamental.propertyClasses[label]?.let { propertyClass ->
            PropertyRelation(
                elementClass = propertyClass,
                holderElementClass = holder,
                isPrimary = isPrimary
            )
        }
}

class ModelGraphDtoConverter(val fundamental: Fundamental) {
    fun ModelNodeDto.convert(): ModelNode = ModelNode(elementClass = fundamental.nodeClasses[label])

    fun EdgeRelationDto.convert(modelGraph: ModelGraph): EdgeRelation? =
        safeLet(modelGraph.nodes[id.sourceKey], modelGraph.nodes[id.targetKey]) { source, target ->
            EdgeRelation(
                elementClass = fundamental.edgeClasses[id.label],
                source = source,
                target = target
            )
        }

    fun ModelGraphDto.convert(): ModelGraph {
        val modelGraph = ModelGraph(
            nodes = nodes.map { it.convert() }.toMutableKeySet()
        )
        modelGraph.addEdges(edges.mapNotNull { it.convert(modelGraph) })
        return modelGraph
    }
}

class DataGraphDtoConverter(val schema: Schema) {
    fun DataGraphDto.convert(): DataGraph {
        val dataGraph = DataGraph(schema, nodes.map { it.convert() }.toMutableKeySet())
        dataGraph.addEdges(edges.mapNotNull { it.convert(dataGraph) })
        return dataGraph
    }

    fun NodeInstanceDto.convert(): NodeInstance {
        val nodeClass = schema.getNodeClass(label)
        val properties = makeProperties(properties, nodeClass)
        return NodeInstance(
            elementClass = nodeClass,
            id = id,
            properties = properties
        )
    }

    fun EdgeInstanceDto.convert(dataGraph: DataGraph): EdgeInstance? =
        safeLet(dataGraph.nodes[source], dataGraph.nodes[target]) { sourceNode, targetNode ->
            val edgeClass = schema.getEdgeClass(label)
            val properties = makeProperties(properties, edgeClass)
            return EdgeInstance(
                source = sourceNode,
                target = targetNode,
                elementClass = edgeClass,
                id = id,
                properties = properties
            )
        }

    fun PropertyInstanceDto.convert(holder: ElementClassPropertyHolder): PropertyInstance? {
        val propertyRelation = if (holder is NodeClass) {
            holder.modelNode?.allProperties?.get(label)
        } else holder.properties[label]
        propertyRelation ?: return null
        //TODO переместить конвертацию значения свойства в более подходящее место
        val convertedValue = when (propertyRelation.elementClass.dataType) {
            DataType.STRING -> value
            DataType.CHARACTER -> value?.singleOrNull()
            DataType.BOOLEAN -> value?.toBoolean()
            DataType.INTEGER -> value?.toIntOrNull()
            DataType.LONG -> value?.toLongOrNull()
            DataType.SHORT -> value?.toShortOrNull()
            DataType.BYTE -> value?.toByteOrNull()
            DataType.DOUBLE -> value?.toDoubleOrNull()
            DataType.DATE -> TODO()
            DataType.GEOSHAPE -> TODO()
            DataType.UUID -> TODO()
        }
        return PropertyInstance(
            modelElement = propertyRelation,
            value = convertedValue
        )
    }

    fun convertValue(value: String?, dataType: DataType): Any? = when (dataType) {
        DataType.STRING -> value
        DataType.CHARACTER -> value?.singleOrNull()
        DataType.BOOLEAN -> value?.toBoolean()
        DataType.INTEGER -> value?.toIntOrNull()
        DataType.LONG -> value?.toLongOrNull()
        DataType.SHORT -> value?.toShortOrNull()
        DataType.BYTE -> value?.toByteOrNull()
        DataType.DOUBLE -> value?.toDoubleOrNull()
        DataType.DATE -> TODO()
        DataType.GEOSHAPE -> TODO()
        DataType.UUID -> TODO()
    }

    private fun makeProperties(
        properties: Set<PropertyInstanceDto>,
        holder: ElementClassPropertyHolder?
    ): MutableKeySet<String, PropertyInstance> =
        (if (holder != null) properties.mapNotNull { it.convert(holder) } else emptyList()).toMutableKeySet()
}