package icu.takeneko.libNeko.nyatwork.util

import java.util.*
import java.util.function.Function

fun interface Reader<T: Any> : Function<FriendlyByteBuf, T> {
    fun asOptional(): Reader<Optional<T>> {
        return Reader { buf: FriendlyByteBuf ->
            buf.readOptional(
                this
            )
        }
    }
}