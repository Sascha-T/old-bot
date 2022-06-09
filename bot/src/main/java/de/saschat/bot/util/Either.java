package de.saschat.bot.util;

import java.util.Optional;

public class Either<A, B> extends Duo<Optional<A>, Optional<B>> {
    public Either(Optional<A> aOptional, Optional<B> bOptional) {
        super(aOptional, bOptional);
    }

    public static<X, Y> Either<X, Y> ofLeft(X left) {
        return new Either<X, Y>(Optional.of(left), Optional.empty());
    }
    public static<X, Y> Either<X, Y> ofRight(Y right) {
        return new Either<X, Y>(Optional.empty(), Optional.of(right));
    }
}
