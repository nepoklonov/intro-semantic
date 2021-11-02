package dto

interface Entity

interface Dto : Convertible

interface Convertible

interface ConvertibleToDto<T : Dto> : Convertible {
    fun convert(): T
}

interface StructureDto : Dto

interface OverallElementDto : Dto

interface SchemaElementDto: OverallElementDto

interface PropertyHolderDto: OverallElementDto{
    val properties: PropertyDto
}

interface NodeDto : OverallElementDto

interface EdgeDto : OverallElementDto

interface PropertyDto : OverallElementDto{
    val key: String
    val label: String
}

interface GraphElementDto : OverallElementDto {
    val id: Any
}

interface GraphDto : StructureDto {
    val nodes: Collection<NodeDto>
    val edges: Collection<EdgeDto>
}