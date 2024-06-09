package icu.takeneko.libNeko.nyatwork;

import java.util.Optional;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
    default Writer<Optional<T>> asOptional() {
        return (buf, optional) -> {
            buf.writeOptional(optional, this);
        };
    }
}

