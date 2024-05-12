package com.cockroachlabs;

/**
 * Incoming request to reduce {@link #name}-based {@link Item} by {@link #reduceBy}.
 *
 * @author Greg L. Turnquist
 */
record ReduceByDTO(String name, int reduceBy) {
}
