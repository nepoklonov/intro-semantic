package dto

data class UserDto(
    var id: Long?,
    var name: String?,
    var password: String?,
    val roles: MutableSet<Role> = mutableSetOf()
)

enum class Role {
    ADMINISTRATOR,
    ONTOLOGIST,
    USER;
}

