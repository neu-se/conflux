package edu.gmu.swe.phosphor;

@FunctionalInterface
public interface Function<U, V> {
    V apply(U operand);
}
