import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.PropertyRelation
import events.*
import specifications.DataType
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

        val age = PropertyInstance(humanAgeRelation, value = 21)
        val human = NodeInstance(humanClass, properties = mutableKeySetOf(age))

        val mutation = Mutation(
            listOf(
                AddEvent(human),
                ChangeDataElementEvent(human, removedProperties = listOf(age)),
                RemoveDataElementEvent(human)
            )
        )
        assertTrue(mutation.events.isEmpty())
    }
}