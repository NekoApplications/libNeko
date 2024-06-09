package icu.takeneko.libNeko.nyatwork;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface Reader<T> extends Function<FriendlyByteBuf, T> {
    default Reader<Optional<T>> asOptional() {
        return (buf) -> {
            return buf.readOptional(this);
        };
    }
}