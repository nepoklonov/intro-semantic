package elements

import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.ModelNode
import kotlin.random.Random

object IdGenerator {
//    fun generateModelNodeId(nodeClassId: String?) = nodeClassId ?: ""
//    fun generateEdgeRelationId(edgeClassId: String?, sourceId: String, targetId: String) = "$edgeClassId.$sourceId.$targetId"
//
//    fun generatePropertyRelationId(propertyClassLabel: String, holderElementClassLabel: String) =
//        "$propertyClassLabel.$holderElementClassLabel"

//    fun generateElementClassId(label: String) = "${now()}-${label.hashCode()}-${Random.nextLong()}"

    fun generateId() = Random.nextLong().toString() //TODO make it correct
}

object EdgeRelationIdManager {

    fun generateId(edgeClass: EdgeClass, sourceNode: ModelNode, targetNode: ModelNode) =
        "${edgeClass.id}_${sourceNode.id}_${targetNode.id}_${Random.nextLong()}"

    fun parseId(id: String): EdgeRelationId {
        val split = id.split(".")
        if (split.size >= 3){
            return EdgeRelationId(split[0], split[1], split[2])
        } else throw IllegalArgumentException("wrong edgeRelation id: $id")
    }

}

data class EdgeRelationId(
    val edgeClassId: String,
    val sourceId: String,
    val targetId: String
)

object PropertyRelationIdManager {

    fun generateId (propertyClass: PropertyClass) = "${propertyClass.id}_${Random.nextLong()}"

    fun parseId(id: String): PropertyRelationId {
        val split = id.split(".")
        if (split.isNotEmpty()){
            return PropertyRelationId(split[0])
        } else throw IllegalArgumentException()
    }
}

class PropertyRelationId(
    val propertyClassId: String
)


fun generateEdgeRelationKey(
    edgeClass: EdgeClass,
    source: ModelNode,
    target: ModelNode
) = generateEdgeRelationKey(edgeClass.key, source.key, target.key)

fun generateEdgeRelationKey(
    edgeClassKey: String,
    sourceKey: String,
    targetKey: String
) = "$sourceKey.$targetKey.$edgeClassKey"