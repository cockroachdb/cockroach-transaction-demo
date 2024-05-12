package com.cockroachlabs;

/**
 * DTO used to return back to the API a representation of an {@link Item}'s {@literal name} and it's current
 * {@literal quantity}.
 * 
 * @author Greg L. Turnquist
 */
record ItemQuantityDTO(String name, int quantity) {
}
