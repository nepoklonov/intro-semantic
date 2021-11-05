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
import utils.unsafeCast
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
    fun `adding, changing and removing edge`() {
        val humanClass = NodeClass("Человек")
        val ageClass = PropertyClass("Возраст", DataType.INTEGER)
        val experienceClass = PropertyClass("Возраст", DataType.INTEGER)
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
        val worksStartedRelation = PropertyRelation(startedClass, worksClass)
        worksClass.properties += worksStartedRelation

        val schema = Schema(
            Fundamental(
                nodeClasses = listOf(humanClass, companyClass),
                edgeClasses = listOf(worksClass),
                propertyClasses = listOf(ageClass, experienceClass, foundedClass, startedClass)
            ),
            ModelGraph(
                nodes = listOf(ModelNode(humanClass)),
                edges = listOf(EdgeRelation(
                    source = ModelNode(humanClass),
                    target = ModelNode(companyClass),
                    elementClass = worksClass
                ))
            )
        )

        val age = PropertyInstance(humanAgeRelation, value = 21)
        val experience = PropertyInstance(humanAgeRelation, value = 5)
        val human = NodeInstance(humanClass, properties = mutableKeySetOf(age))

        val foundedA = PropertyInstance(companyFoundedRelation, value = 1999)
        val foundedB = PropertyInstance(companyFoundedRelation, value = 1999)
        val companyA = NodeInstance(companyClass, properties = mutableKeySetOf(foundedA))
        val companyB = NodeInstance(companyClass, properties = mutableKeySetOf(foundedB))

        val startedA = PropertyInstance(worksStartedRelation, value = 2016)
        val worksA = EdgeInstance(human, companyA, worksClass, properties = mutableKeySetOf(startedA))
        val startedB = PropertyInstance(worksStartedRelation, value = 2020)
        val worksB = EdgeInstance(human, companyB, worksClass, properties = mutableKeySetOf(startedB))

        val dataFrame = DataGraphFrame(DataGraph(schema))

        dataFrame.addNode(human)
        dataFrame.addNode(companyA)
        dataFrame.addEdge(worksA)
        dataFrame.changeElement(worksA, propertiesToRemove = listOf(startedA))
        dataFrame.changeElement(human, propertiesToRemove = listOf(age))
        dataFrame.changeElement(human, propertiesToAdd = listOf(experience))
        dataFrame.removeNode(companyA)
        dataFrame.addNode(companyB)
        dataFrame.addEdge(worksB)
        dataFrame.changeElement(worksB, propertiesToRemove = listOf(startedB))

        /*
         * Should leave:
         * AddEvent (human, changed)
         * AddEvent (companyB)
         * AddEvent (worksB, changed)
         */

        val unoptimizedEvents = dataFrame.eventLine.events.map { it.originalEvent }
        val mutation = Mutation(unoptimizedEvents)
        assertTrue(mutation.addEvents.size == 3)
        assertTrue(mutation.removeEvents.isEmpty())
        assertTrue(mutation.changeEvents.isEmpty())
        assertTrue {
            @Suppress("UNCHECKED_CAST")
            when (mutation.addEvents[0].elementForm) {
                ElementForm.NODE -> {
                    val addNodeEvent = mutation.addEvents[1] as AddEvent<NodeInstance, NodeInstanceDto>
                    addNodeEvent.dto.properties.isNotEmpty()
                }
                ElementForm.EDGE -> false
                ElementForm.PROPERTY -> false
            }
        }
    }
}