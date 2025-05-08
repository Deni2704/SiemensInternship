
package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemServiceImpl implements ItemService{
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
        public Item save(Item item) {
            Validator validator = new Validator(item);
            if (!validator.validate()) {
                throw new IllegalArgumentException(validator.getFormattedErrors());
            }
            return itemRepository.save(item);
        }


    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Override
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // Retrieve all item IDs from the database to process each item asynchronously
        List<Long> itemIds = itemRepository.findAllIds();

        // updating its status to 'PROCESSED', and saving it back to the database
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return itemRepository.findById(id)
                                .map(item -> {
                                    item.setStatus("PROCESSED");
                                    return itemRepository.save(item);
                                }).orElse(null);
                    } catch (Exception e) {
                        System.err.println("Error processing item " + id + ": " + e.getMessage());
                        return null;
                    }
                }, executor))
                .toList();

        // Wait for all asynchronous tasks to complete and collect the results
        // 'join' will block until each CompletableFuture completes,
        List<Item> processedItems = futures.stream()
                .map(CompletableFuture::join)
                .filter(item -> item != null)
                .toList();

        return CompletableFuture.completedFuture(processedItems);
    }
}