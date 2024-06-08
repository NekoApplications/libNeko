package icu.takeneko.libNeko.registry

class Registry<E>(val id: Identifier) {
    private val map = mutableMapOf<Identifier, E>()
    private val keyMap = mutableMapOf<E, Identifier>()
    var frozen = false
        private set

    fun register(k: Identifier, elem: E) {
        if (frozen) throw IllegalStateException("Registry $id already frozen!")
        map[k] = elem
        keyMap[elem] = k
    }

    fun getKey(value: E): Identifier? {
        return keyMap[value]
    }

    fun get(name: Identifier): E? {
        return map[name]
    }

    fun freeze() {
        frozen = true
    }
}