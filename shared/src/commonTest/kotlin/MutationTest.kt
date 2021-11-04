import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
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
    fun `adding, changing and removing edge`() {
        val humanClass = NodeClass("Человек")
        val ageClass = PropertyClass("Возраст", DataType.INTEGER)
        val humanAgeRelation = PropertyRelation(ageClass, humanClass)
        humanClass.properties += humanAgeRelation

        val worksClass = EdgeClass("Работает")

        val schema = Schema(
            Fundamental(
                nodeClasses = listOf(humanClass),
                edgeClasses = listOf(worksClass),
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
}