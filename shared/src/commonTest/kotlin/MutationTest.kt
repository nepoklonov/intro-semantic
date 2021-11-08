import dto.EdgeInstanceDto
import dto.NodeInstanceDto
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import events.*
import events.frames.DataGraphFrame
import specifications.DataType
import structure.DataGraph
import structure.Fundamental
import structure.ModelGraph
import structure.Schema
import utils.collections.mutableKeySetOf
import kotlin.test.Test
import kotlin.test.assertTrue

class MutationTest {
    @Test
    fun `adding, changing and removing element`() {
        val humanClass = NodeClass("Человек")
        val ageClass = PropertyClass("Возраст", DataType.INTEGER)
        val humanAgeRelation = PropertyRelation(ageClass, humanClass)
        humanClass.properties += humanAgeRelation

        val schema = Schema(
            Fundamental(
                nodeClasses = listOf(humanClass),
                propertyClasses = listOf(ageClass)
            ),
            ModelGraph(
                nodes = listOf(ModelNode(humanClass))
            )
        )

        val age = PropertyInstance(humanAgeRelation, value = 21)
        val human = NodeInstance(humanClass, properties = mutableKeySetOf(age))

        val dataFrame = DataGraphFrame(DataGraph(schema))

        dataFrame.addNode(human)
        dataFrame.changeElement(human, propertiesToRemove = listOf(age))
        dataFrame.removeNode(human)

        val unoptimizedEvents = dataFrame.eventLine.events.map { it.originalEvent }
        val mutation = Mutation(unoptimizedEvents)
        assertTrue(mutation.events.isEmpty())
    }

    @Test
    fun complexTest() {
        val humanClass = NodeClass("Человек")
        val ageClass = PropertyClass("Возраст", DataType.INTEGER)
        val experienceClass = PropertyClass("Опыт", DataType.INTEGER)
        val humanAgeRelation = PropertyRelation(ageClass, humanClass)
        val humanExperienceRelation = PropertyRelation(experienceClass, humanClass)
        humanClass.properties += humanAgeRelation
        humanClass.properties += humanExperienceRelation

        val companyClass = NodeClass("Компания")
        val foundedClass = PropertyClass("Основана", DataType.INTEGER)
        val companyFoundedRelation = PropertyRelation(foundedClass, companyClass)
        companyClass.properties += companyFoundedRelation

        val worksClass = EdgeClass("Работает")
        val startedClass = PropertyClass("Начал работать", DataType.INTEGER)
        val awardedClass = PropertyClass("Награждён", DataType.INTEGER)
        val worksStartedRelation = PropertyRelation(startedClass, worksClass)
        val worksAwardedRelation = PropertyRelation(awardedClass, worksClass)
        worksClass.properties += worksStartedRelation
        worksClass.properties += worksAwardedRelation

        val schema = Schema(
            Fundamental(
                nodeClasses = listOf(humanClass, companyClass),
                edgeClasses = listOf(worksClass),
                propertyClasses = listOf(ageClass, experienceClass, foundedClass, startedClass, awardedClass)
            ),
            ModelGraph(
                nodes = listOf(ModelNode(humanClass)),
                edges = listOf(
                    EdgeRelation(
                        source = ModelNode(humanClass),
                        target = ModelNode(companyClass),
                        elementClass = worksClass
                    )
                )
            )
        )

        val age = PropertyInstance(humanAgeRelation, value = 21)
        val experience = PropertyInstance(humanExperienceRelation, value = 5)
        val human = NodeInstance(humanClass, properties = mutableKeySetOf(age))

        val foundedA = PropertyInstance(companyFoundedRelation, value = 1999)
        val foundedB = PropertyInstance(companyFoundedRelation, value = 1999)
        val companyA = NodeInstance(companyClass, properties = mutableKeySetOf(foundedA))
        val companyB = NodeInstance(companyClass, properties = mutableKeySetOf(foundedB))

        val startedA = PropertyInstance(worksStartedRelation, value = 2016)
        val worksA = EdgeInstance(human, companyA, worksClass, properties = mutableKeySetOf(startedA))
        val startedB = PropertyInstance(worksStartedRelation, value = 2020)
        val awardedB = PropertyInstance(worksAwardedRelation, value = 2021)
        val worksB = EdgeInstance(source = human, target = companyB, worksClass, properties = mutableKeySetOf(startedB))

        val dataFrame = DataGraphFrame(DataGraph(schema))

        dataFrame.addNode(human)
        dataFrame.addNode(companyA)
        dataFrame.addEdge(worksA)
        dataFrame.changeElement(worksA, propertiesToRemove = listOf(startedA))
        dataFrame.changeElement(worksA, propertiesToAdd = listOf(startedA))
        dataFrame.changeElement(worksA, propertiesToRemove = listOf(startedA))
        dataFrame.changeElement(worksA, propertiesToAdd = listOf(startedA), propertiesToRemove = listOf(startedA))
        dataFrame.changeElement(worksA, propertiesToAdd = listOf(startedA))
        dataFrame.changeElement(worksA, propertiesToRemove = listOf(startedA))
        dataFrame.changeElement(human, propertiesToRemove = listOf(age))
        dataFrame.changeElement(human, propertiesToAdd = listOf(experience))
        dataFrame.removeNode(companyA)
        dataFrame.addNode(companyB)
        dataFrame.addEdge(worksB)
        dataFrame.reverseEdge(worksB)
        dataFrame.changeElement(worksB, propertiesToAdd = listOf(awardedB))
        dataFrame.changeElement(worksB, propertiesToRemove = listOf(startedB))

        /*
         * Should leave:
         * AddEvent (human, properties = experience)
         * AddEvent (companyB)
         * AddEvent (worksB, properties = awardedB, target = human, source = companyB)
         *
         * Test cases:
         * addEvent         -> ... -- passed
         * changeEvent      -> ... -- passed
         * compositeEvent   -> ... -- passed
         * reverseEdgeEvent -> ... -- passed */

        val unoptimizedEvents1 = dataFrame.eventLine.events.map { it.originalEvent }
        val mutation1 = Mutation(unoptimizedEvents1)

        assertTrue(mutation1.addEvents.size == 3)
        assertTrue(mutation1.removeEvents.isEmpty())
        assertTrue(mutation1.changeEvents.isEmpty())
        assertTrue(mutation1.reverseEdgeEvents.isEmpty())

        assertTrue { // Human addEvent has experience property, but not age property -- passed
            @Suppress("UNCHECKED_CAST")
            when (mutation1.addEvents[0].elementForm) {
                ElementForm.NODE -> {
                    val addNodeEvent = mutation1.addEvents[0] as AddEvent<NodeInstance, NodeInstanceDto>
                    addNodeEvent.dto.properties.contains(experience.convert()) &&
                            !addNodeEvent.dto.properties.contains(age.convert())
                }
                ElementForm.EDGE -> false
                ElementForm.PROPERTY -> false
            }
        }

        assertTrue { // CompanyB addEvent has foundedB property -- passed
            @Suppress("UNCHECKED_CAST")
            when (mutation1.addEvents[1].elementForm) {
                ElementForm.NODE -> {
                    val addNodeEvent = mutation1.addEvents[1] as AddEvent<NodeInstance, NodeInstanceDto>
                    addNodeEvent.dto.properties.contains(foundedB.convert())
                }
                ElementForm.EDGE -> false
                ElementForm.PROPERTY -> false
            }
        }

        assertTrue { // WorksB addEvent has awardedB property, but not startedB property
            // source is company and target is human (reversed) -- passed
            @Suppress("UNCHECKED_CAST")
            when (mutation1.addEvents[2].elementForm) {
                ElementForm.NODE -> false
                ElementForm.EDGE -> {
                    val addEdgeEvent = mutation1.addEvents[2] as AddEvent<EdgeInstance, EdgeInstanceDto>
                    addEdgeEvent.dto.properties.contains(awardedB.convert()) &&
                            !addEdgeEvent.dto.properties.contains(startedB.convert()) &&
                            addEdgeEvent.dto.target == human.id &&
                            addEdgeEvent.dto.source == companyB.id
                }
                ElementForm.PROPERTY -> false
            }
        }

        val unoptimizedEvents2 = dataFrame.eventLine.events.drop(3).dropLast(6).map { it.originalEvent }
        val mutation2 = Mutation(unoptimizedEvents2)

        /*
        * Should leave:
        * ChangeEvent (worksA, propertiesToRemove = listOf(startedA))
        * ChangeEvent (human, propertiesToRemove = listOf(age), propertiesToAdd = listOf(experience)) */

        assertTrue(mutation2.events.size == 2)
        assertTrue(mutation2.changeEvents.size == 2)
        assertTrue(mutation2.addEvents.isEmpty())
        assertTrue(mutation2.removeEvents.isEmpty())
        assertTrue(mutation2.reverseEdgeEvents.isEmpty())

        assertTrue { // WorksA removes only startedA property -- passed
            val changeDataElementEvent = mutation2.changeEvents[0] as ChangeDataElementEvent
            changeDataElementEvent.propertyChanges.propertyKeysToRemove.contains(startedA.key) &&
                    changeDataElementEvent.propertyChanges.propertyKeysToRemove.size == 1 &&
                    changeDataElementEvent.propertyChanges.propertiesToAdd.size != 1
        }

        assertTrue { // Human removes age property and adds experience -- passed
            val changeDataElementEvent = mutation2.changeEvents[1] as ChangeDataElementEvent
            changeDataElementEvent.propertyChanges.propertyKeysToRemove.contains(age.key) &&
                    changeDataElementEvent.propertyChanges.propertiesToAdd.contains(experience.convert())
        }

        val unoptimizedEvents3 = dataFrame.eventLine.events.drop(3).dropLast(5).map { it.originalEvent }
        val mutation3 = Mutation(unoptimizedEvents3)

        /*
        * Should leave:
        * ChangeEvent (human, propertiesToRemove = listOf(age), propertiesToAdd = listOf(experience))
        * RemoveEvent (worksA)
        * RemoveEvent (companyA) */

        assertTrue(mutation3.events.size == 3)
        assertTrue(mutation3.changeEvents.size == 1)
        assertTrue(mutation3.addEvents.isEmpty())
        assertTrue(mutation3.removeEvents.size == 2)
        assertTrue(mutation3.reverseEdgeEvents.isEmpty())

        assertTrue { // Human removes age property and adds experience -- passed
            val changeDataElementEvent = mutation3.changeEvents[0] as ChangeDataElementEvent
            changeDataElementEvent.propertyChanges.propertyKeysToRemove.contains(age.key) &&
                    changeDataElementEvent.propertyChanges.propertiesToAdd.contains(experience.convert())
        }

        assertTrue { // Remove worksA -- passed
            val removeEvent = mutation3.removeEvents[0] as RemoveDataElementEvent
            removeEvent.elementKey == worksA.key
        }

        assertTrue { // Remove companyA -- passed
            val removeEvent = mutation3.removeEvents[1] as RemoveDataElementEvent
            removeEvent.elementKey == companyA.key
        }

        val unoptimizedEvents4 = dataFrame.eventLine.events.takeLast(3).map { it.originalEvent }
        val mutation4 = Mutation(unoptimizedEvents4)

        /*
        * Should leave:
        * ChangeEvent (worksB, propertiesToRemove = listOf(startedB), propertiesToAdd = listOf(awardedB))
        * ReverseEdgeEvent (worksB) */

        assertTrue(mutation4.events.size == 2)
        assertTrue(mutation4.changeEvents.size == 1)
        assertTrue(mutation4.addEvents.isEmpty())
        assertTrue(mutation4.removeEvents.isEmpty())
        assertTrue(mutation4.reverseEdgeEvents.size == 1)

        assertTrue { // WorksB removes age property and adds experience -- passed
            val changeDataElementEvent = mutation4.changeEvents[0] as ChangeDataElementEvent
            changeDataElementEvent.propertyChanges.propertyKeysToRemove.contains(startedB.key) &&
                    changeDataElementEvent.propertyChanges.propertiesToAdd.contains(awardedB.convert())
        }

        assertTrue { // WorksB is reversed -- passed
            val reverseEdgeEvent = mutation4.reverseEdgeEvents[0]
            reverseEdgeEvent.elementKey == worksB.key
        }

        dataFrame.reverseEdge(worksB)

        val unoptimizedEvents5 = dataFrame.eventLine.events.takeLast(4).map { it.originalEvent }
        val mutation5 = Mutation(unoptimizedEvents5)

        /*
        * Should leave:
        * ChangeEvent (worksB, propertiesToRemove = listOf(startedB), propertiesToAdd = listOf(awardedB)) */

        assertTrue(mutation5.events.size == 1)
        assertTrue(mutation5.changeEvents.size == 1)
        assertTrue(mutation5.addEvents.isEmpty())
        assertTrue(mutation5.removeEvents.isEmpty())
        assertTrue(mutation5.reverseEdgeEvents.isEmpty())

        assertTrue { // WorksB removes age property and adds experience -- passed
            val changeDataElementEvent = mutation5.changeEvents[0] as ChangeDataElementEvent
            changeDataElementEvent.propertyChanges.propertyKeysToRemove.contains(startedB.key) &&
                    changeDataElementEvent.propertyChanges.propertiesToAdd.contains(awardedB.convert())
        }

        dataFrame.removeNode(companyB)

        val unoptimizedEvents6 = dataFrame.eventLine.events.takeLast(4).map { it.originalEvent }
        val mutation6 = Mutation(unoptimizedEvents6)

        /*
        * Should leave:
        * RemoveEvent (companyB)
        * RemoveEvent (worksB) */

        assertTrue(mutation6.events.size == 2)
        assertTrue(mutation6.changeEvents.isEmpty())
        assertTrue(mutation6.addEvents.isEmpty())
        assertTrue(mutation6.removeEvents.size == 2)
        assertTrue(mutation6.reverseEdgeEvents.isEmpty())

        assertTrue { // Remove worksB -- passed
            val removeEvent = mutation6.removeEvents[0] as RemoveDataElementEvent
            removeEvent.elementKey == worksB.key
        }

        assertTrue { // Remove companyB -- passed
            val removeEvent = mutation6.removeEvents[1] as RemoveDataElementEvent
            removeEvent.elementKey == companyB.key
        }

    }
}