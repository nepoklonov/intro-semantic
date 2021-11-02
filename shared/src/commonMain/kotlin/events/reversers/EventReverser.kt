package events.reversers

import dto.PropertyDto
import elements.OverallElement
import elements.abstract.Edge
import elements.abstract.Node
import events.AtomicEvent
import events.CompositeEvent
import events.Event
import events.PropertyChanges
import structure.Graph

abstract class EventReverser<N : Node<N, E>, E : Edge<N, E>> {

    protected fun <T : OverallElement, X : PropertyDto> revertPropertyChanges(
        propertyChanges: PropertyChanges<X>,
        element: T
    ): PropertyChanges<X> {
        val propertiesToAdd = mutableListOf<X>()
        val propertyIdsToRemove = mutableListOf<String>()

        val revertedIdsToRemove = propertyChanges.propertyKeysToRemove
            .mapNotNull { getPropertyDtoByKeyFromElement<T, X>(element, it) }
        propertiesToAdd.addAll(revertedIdsToRemove)

        propertyChanges.propertiesToAdd.forEach { originalDto ->
            getPropertyDtoByKeyFromElement<T, X>(element, originalDto.key)?.let { reversedDto ->
                propertiesToAdd.add(reversedDto)
            } ?: propertyIdsToRemove.add(originalDto.key)
        }

        return PropertyChanges(propertiesToAdd, propertyIdsToRemove)
    }

    protected abstract fun <T : OverallElement, X : PropertyDto> getPropertyDtoByKeyFromElement(
        element: T,
        key: String
    ): X?

}