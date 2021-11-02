package events.eventlines

import elements.data.EdgeInstance
import elements.data.NodeInstance
import events.appliers.DataGraphEventApplier
import events.reversers.DataGraphEventReverser

class DataGraphEventLine(
    override val eventApplier: DataGraphEventApplier
) : EventLine<NodeInstance, EdgeInstance>()