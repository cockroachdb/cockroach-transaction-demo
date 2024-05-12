package com.cockroachlabs;

/**
 * A {@link RuntimeException} that captures the failure to reduce a given item's stock supply.
 * 
 * @author Greg L. Turnquist
 */
class InsufficientInventory extends RuntimeException {

	InsufficientInventory(String name, int quantity, int requestedAmount) {
		super("Insufficient inventory to purchase '" + name + "' (quantity: " + quantity + ", requested: " + requestedAmount
				+ ")");
	}
}
