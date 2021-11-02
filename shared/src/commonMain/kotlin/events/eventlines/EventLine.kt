package events.eventlines

import elements.abstract.Edge
import elements.abstract.Node
import events.Event
import events.EventPair
import events.Mutation
import events.appliers.EventApplier
import events.reversers.EventReverser

const val POINTER_INITIAL_VALUE = -1

abstract class EventLine<N : Node<N, E>, E : Edge<N, E>> {
    val events: List<EventPair<*, *>>
        get() = _events
    private val _events: MutableList<EventPair<*, *>> = mutableListOf()

    private val pointerMaxValue
        get() = _events.size - 1

    var pointer: Int = POINTER_INITIAL_VALUE
        private set


    abstract val eventApplier: EventApplier<N, E>

    open fun undo() {
        if (pointer > POINTER_INITIAL_VALUE) {
            eventApplier.applyEvent(events[pointer--].reversedEvent)
        }
    }

    open fun redo() {
        if (pointer < pointerMaxValue) {
            eventApplier.applyEvent(events[++pointer].originalEvent)
        }
    }

    fun updateStructure(mutation: Mutation): Boolean {
        _events.clear()
        pointer = POINTER_INITIAL_VALUE

        return !mutation.events.map { eventApplier.applyEvent(it) }.contains(false)
    }

    fun addEvent(eventPair: EventPair<*, *>) {
        if (pointer != pointerMaxValue) {
            while (pointer < pointerMaxValue) {
                _events.removeAt(pointer + 1)
            }
        }
        _events.add(eventPair)
        pointer = pointerMaxValue
    }


    fun addAndApply(eventPair: EventPair<*, *>): Boolean {
        addEvent(eventPair)
        return eventApplier.applyEvent(eventPair.originalEvent)
    }
}