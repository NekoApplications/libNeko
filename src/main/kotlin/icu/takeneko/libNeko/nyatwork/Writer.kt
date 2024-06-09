package icu.takeneko.libNeko.nyatwork

import java.util.*
import java.util.function.BiConsumer

fun interface Writer<T: Any> : BiConsumer<FriendlyByteBuf, T> {
    fun asOptional(): Writer<Optional<T>> {
        return Writer { buf: FriendlyByteBuf, optional: Optional<T> ->
            buf.writeOptional(optional, this)
        }
    }
}

