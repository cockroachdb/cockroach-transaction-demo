package com.cockroachlabs;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data repository used to interact with {@link Item}s.
 *
 * @author Greg L. Turnquist
 */
interface ItemRepository extends JpaRepository<Item, UUID> {

	Item findByName(String name);

	@Modifying
	@Query("""
			UPDATE Item item
			SET item.quantity = item.quantity - :requestedAmount
			WHERE item.name = :name
			""")
	void reduceQuantity(String name, int requestedAmount);

	/**
	 * This operation will fetch the current "quantity" and then check if it's enough before updating the stock.
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	default void updateItemInventoryWithReadCommittedTransaction(String name, int requestedAmount) {

		int quantity = findByName(name).getQuantity();

		// This is the place where you could possibly have the user "confirm" the order before proceeding with the UPDATE

		if (quantity > requestedAmount) {
			reduceQuantity(name, requestedAmount);
		} else {
			throw new InsufficientInventory(name, quantity, requestedAmount);
		}
	}

	/**
	 * The only difference between this transaction and the previous one is the isolation level. Cockroach defaults to
	 * isolation level SERIALIZABLE.
	 */
	@Transactional
	default void updateItemInventoryWithSerializableTransaction(String name, int requestedAmount) {

		int quantity = findByName(name).getQuantity();

		// This is the place where you could possibly have the user "confirm" the order before proceeding with the UPDATE

		if (quantity > requestedAmount) {
			reduceQuantity(name, requestedAmount);
		} else {
			throw new InsufficientInventory(name, quantity, requestedAmount);
		}
	}

}
