package com.cockroachlabs;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * A service bean used for conducting {@link Item}-based operations.
 *
 * @author Greg L. Turnquist
 */
@Service
class ItemInventoryService {

	private static final Logger log = LoggerFactory.getLogger(ItemInventoryService.class);

	private final ItemRepository repository;
	private final ItemRepositoryWithSelectForUpdate repository2;

	ItemInventoryService(ItemRepository repository, ItemRepositoryWithSelectForUpdate repository2) {

		this.repository = repository;
		this.repository2 = repository2;
	}

	/**
	 * An operation that reduces the inventory for a given item using {@literal READ COMMITTED} isolation level. NOTE:
	 * This operation isn't transactional, The underlying repository operation is transactional.
	 */
	void updateItemInventoryWithReadCommittedTransaction(String name, int requestedAmount) {

		repository.updateItemInventoryWithReadCommittedTransaction(name, requestedAmount);

		// Since READ COMMITTED will allow the quantity to be overdrawn, this is where you might need
		// additional handling to make sure either the order CAN be fulfilled, or the customer's order
		// has to be amended in some way.
	}

	/**
	 * An operation that reduces the inventory for a given item using Cockroach's default isolation level
	 * ({@literal SERIALIZABLE}). NOTE: This operation isn't transactional. The underlying repository operation is
	 * transactional. This allows the operation to be attempted multiple times.
	 */
	void updateItemInventoryWithSerializableTransaction(String name, int requestedAmount) {

		while (true) { // Is it okay to try an infinite number of times, or should you limit this?
			try {
				repository.updateItemInventoryWithSerializableTransaction(name, requestedAmount);
				return;
			} catch (DataAccessException exception) {

				// "40001" is the SQLState code for WriteTooOldError,
				// a signal of a SERIALIZABLE failure that should be retried.
				if (exception.getRootCause() instanceof PSQLException psqlException
						&& psqlException.getSQLState().equals("40001")) {

					log.error("ENCOUNTERED: " + psqlException);

					try {
						Thread.sleep(100L); // Is a fixed delay the best approach?
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					// NOTE: If it's not a SQLState "40001", then we don't want to retry the transaction.
					throw exception;
				}
			}
		}
	}

	void updateItemInventoryWithSelectForUpdate(String name, int requestedAmount) {

		while (true) { // Is it okay to try an infinite number of times, or should you limit this?
			try {
				repository2.updateItemInventory(name, requestedAmount);
				return;
			} catch (DataAccessException exception) {

				// "40001" is the SQLState code for WriteTooOldError,
				// a signal of a SERIALIZABLE failure that should be retried.
				if (exception.getRootCause() instanceof PSQLException psqlException
						&& psqlException.getSQLState().equals("40001")) {

					log.error("ENCOUNTERED: " + psqlException);

					try {
						Thread.sleep(100L); // Is a fixed delay the best approach?
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					// NOTE: If it's not a SQLState "40001", then we don't want to retry the transaction.
					throw exception;
				}
			}
		}
	}
}
