package com.cockroachlabs;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

/**
 * Spring MVC controller to model certain API-based operations to interact with the Kwikshoppr system.
 *
 * @author Greg L. Turnquist
 */
@RestController
class ApiController {

	private final ItemRepository repository;
	private final ItemInventoryService service;

	ApiController(ItemRepository repository, ItemInventoryService service) {

		this.repository = repository;
		this.service = service;
	}

	@GetMapping("/api/items")
	List<Item> all() {
		return repository.findAll();
	}

	@GetMapping("/api/items/{id}")
	Item item(@PathVariable UUID id) {
		return repository.findById(id) //
				.orElseThrow(() -> new RuntimeException("Can't find item '" + id + "'"));
	}

	@GetMapping("/api/quantity/{name}")
	ItemQuantityDTO item(@PathVariable String name) {
		return new ItemQuantityDTO(name, repository.findByName(name).getQuantity());
	}

	@PostMapping("/api/reduceQuantity/readCommitted")
	void reduceQuantityReadCommitted(@RequestBody ReduceByDTO reduceBy) {
		service.updateItemInventoryWithReadCommittedTransaction(reduceBy.name(), reduceBy.reduceBy());
	}

	@PostMapping("/api/reduceQuantity/serializable")
	void reduceQuantitySerializable(@RequestBody ReduceByDTO reduceBy) {
		service.updateItemInventoryWithSerializableTransaction(reduceBy.name(), reduceBy.reduceBy());
	}

	@PostMapping("/api/reduceQuantity/selectForUpdate")
	void reduceQuantitySelectForUpdate(@RequestBody ReduceByDTO reduceBy) {
		service.updateItemInventoryWithSelectForUpdate(reduceBy.name(), reduceBy.reduceBy());
	}
}
