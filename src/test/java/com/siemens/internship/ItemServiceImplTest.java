package com.siemens.internship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@SpringBootTest
public class ItemServiceImplTest {
    @Autowired
    private ItemServiceImpl itemServiceImpl;

    @BeforeEach
    public void clearDatabase() {
        itemServiceImpl.findAll().forEach(item -> itemServiceImpl.deleteById(item.getId()));
    }

    @Test
    public void testSaveDuplicateItem() {
        Item item1 = new Item(null, "Laptop", "Description", "NEW", "valid@yahoo.com");
        Item savedItem1 = itemServiceImpl.save(item1);

        Item item2 = new Item(null, "Laptop", "Description", "NEW", "valid@yahoo.com");
        Item savedItem2 = itemServiceImpl.save(item2);

        Assertions.assertEquals(savedItem1.getName(), savedItem2.getName(), "Itemele ar trebui să aibă același nume.");
    }
    @Test
    public void testProcessItemsAsync() throws Exception {
        Item item1 = new Item(null, "Item 1", "Description 1", "NEW", "valid1@yahoo.com");
        Item item2 = new Item(null, "Item 2", "Description 2", "NEW", "valid2@yahoo.com");

        itemServiceImpl.save(item1);
        itemServiceImpl.save(item2);

        List<Item> allItems = itemServiceImpl.findAll();
        Assertions.assertEquals(2, allItems.size(), "Ar trebui să existe 2 itemi în baza de date.");

        // AȘTEAPTĂ rezultatul asincron
        List<Item> processedItems = itemServiceImpl.processItemsAsync().get();

        System.out.println("Processed items: " + processedItems);
        Assertions.assertFalse(processedItems.isEmpty());
        Assertions.assertNotNull(processedItems);
        processedItems.forEach(item ->
                Assertions.assertEquals("PROCESSED", item.getStatus(), "Statusul nu este corect pentru itemul procesat.")
        );
    }
    @Test
    public void testProcessLargeNumberOfItemsAsync() throws Exception {
        for (int i = 0; i < 1000; i++) {
            Item item = new Item(null, "Item " + i, "Description", "NEW", "valid" + i + "@example.com");
            itemServiceImpl.save(item);
        }

        List<Item> processedItems = itemServiceImpl.processItemsAsync().get(); // AȘTEPTĂM rezultatul aici

        Assertions.assertEquals(1000, processedItems.size(), "Numărul de itemi procesati nu corespunde.");
    }

    @Test
    public void testProcessSingleItemAsync() throws Exception {
        Item item = new Item(null, "Single Item", "Description", "NEW", "valid@example.com");
        itemServiceImpl.save(item);

        List<Item> processedItems = itemServiceImpl.processItemsAsync().get(); // AȘTEPTĂM rezultatul

        Assertions.assertFalse(processedItems.isEmpty(), "Lista de itemi procesati nu ar trebui să fie goală.");
        Assertions.assertEquals("PROCESSED", processedItems.get(0).getStatus(), "Statusul itemului procesat nu este corect.");
    }
    @Test
    public void testSaveItem() {
        Item item = new Item(null, "Laptop", "Description", "NEW", "valid@yahoo.com");
        Item savedItem = itemServiceImpl.save(item);

        Assertions.assertNotNull(savedItem);
        Assertions.assertNotNull(savedItem.getId());
        Assertions.assertEquals("Laptop", savedItem.getName());
    }
    @Test
    public void testSaveInvalidEmailItem() {
        Item item = new Item(null, "Laptop", "Desc", "NEW", "invalid-email");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            itemServiceImpl.save(item);
        });
    }
    @Test
    public void testDeleteItem() {
        Item item = new Item(2L, "Laptop", "Description", "NEW", "valid@yahoo.com");
        Item savedItem = itemServiceImpl.save(item);

        itemServiceImpl.deleteById(savedItem.getId());

        Optional<Item> deletedItem = itemServiceImpl.findById(savedItem.getId());
        Assertions.assertFalse(deletedItem.isPresent(), "Itemul ar trebui să fie șters din baza de date.");
    }
    @Test
    public void testDeleteItemThatDoesNotExist() {
        Long nonExistentId = 10L;

        itemServiceImpl.deleteById(nonExistentId);

        Optional<Item> deletedItem = itemServiceImpl.findById(nonExistentId);
        Assertions.assertFalse(deletedItem.isPresent(), "Itemul nu a fost găsit, așa cum era de așteptat.");
    }

}
