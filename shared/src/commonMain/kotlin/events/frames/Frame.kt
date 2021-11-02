package events.frames

import elements.abstract.Edge
import elements.abstract.Node
import events.eventlines.EventLine
import structure.Graphable
import structure.Structure

interface Frame<N : Node<N, E>, E : Edge<N, E>> : Graphable<N, E> {
    val structure: Structure
    val eventLine: EventLine<N, E>
}