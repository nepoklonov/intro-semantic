package elements.utils

import elements.ElementClassPropertyHolder
import elements.schema.EdgeClassType
import elements.schema.NodeClassType
import elements.schema.PropertyClassType
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import specifications.Cardinality
import specifications.DataType
import specifications.Multiplicity

fun NodeClass.copy(
    label: String = this.label,
    id: String = this.id,
    type: NodeClassType = this.type
) = NodeClass(label, id, type)

fun EdgeClass.copy(
    label: String = this.label,
    id: String = this.id,
    type: EdgeClassType = this.type,
    multiplicity: Multiplicity = this.multiplicity
) = EdgeClass(label, id, type, multiplicity)

fun PropertyClass.copy(
    label: String = this.label,
    dataType: DataType = this.dataType,
    cardinality: Cardinality = this.cardinality,
    id: String = this.id,
    type: PropertyClassType = this.type,
) = PropertyClass(label, dataType, cardinality, id, type)

fun ModelNode.copy(
    elementClass: NodeClass? = this.elementClass
) = ModelNode(elementClass)

fun EdgeRelation.copy(
    source: ModelNode = this.source,
    target: ModelNode = this.target,
    elementClass: EdgeClass? = this.elementClass
) = EdgeRelation(source, target, elementClass, label)

fun PropertyRelation.copy(
    elementClass: PropertyClass = this.elementClass,
    holderElementClass: ElementClassPropertyHolder = this.holderElementClass,
    isPrimary: Boolean = this.isPrimary
) = PropertyRelation(elementClass, holderElementClass, isPrimary)