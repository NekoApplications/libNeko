package icu.takeneko.libNeko.registry

data class Identifier(val namespace: String, val path: String) {
    constructor(compound: String) : this(
        compound.split(":")[0],
        compound.split(":")[1]
    )

    override fun toString(): String {
        return "$namespace:$path"
    }
}
