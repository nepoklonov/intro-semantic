package elements

import specifications.Multiplicity
import elements.schema.EdgeClassType
import elements.schema.fundamental.EdgeClass
import org.janusgraph.core.JanusGraphVertex
import org.janusgraph.core.schema.JanusGraphManagement

interface ElementClassStatic<T : JanusGraphVertex> {
    fun getAllElements(mgmt: JanusGraphManagement): Iterable<T>
    fun getById(mgmt: JanusGraphManagement, id: String): T?
}

actual object Elements {
    actual val inheritanceEdgeClass: EdgeClass = EdgeClass(
        label = "частный случай",
        type = EdgeClassType.INHERITANCE_EDGE_CLASS,
        multiplicity = Multiplicity.MANY2ONE,
        id = "inheritance"
    )
}
