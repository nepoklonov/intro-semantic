package events.eventlines

import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import events.appliers.SchemaEventApplier
import events.reversers.SchemaEventReverser

class SchemaEventLine(
    override val eventApplier: SchemaEventApplier,
) : EventLine<ModelNode, EdgeRelation>()