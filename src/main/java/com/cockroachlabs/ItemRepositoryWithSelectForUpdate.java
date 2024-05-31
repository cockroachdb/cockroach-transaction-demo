package com.cockroachlabs;

import jakarta.persistence.LockModeType;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

interface ItemRepositoryWithSelectForUpdate extends JpaRepository<Item, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Item findByName(String name);

	@Modifying
	@Query("""
			UPDATE Item item
			SET item.quantity = item.quantity - :requestedAmount
			WHERE item.name = :name
			""")
	void reduceQuantity(String name, int requestedAmount);

	/**
	 * The only difference between this transaction and the previous one is the isolation level. Cockroach defaults to
	 * isolation level SERIALIZABLE.
	 */
	@Transactional
	default void updateItemInventory(String name, int requestedAmount) {

		int quantity = findByName(name).getQuantity();

		if (quantity > requestedAmount) {
			reduceQuantity(name, requestedAmount);
		} else {
			throw new InsufficientInventory(name, quantity, requestedAmount);
		}
	}

}
