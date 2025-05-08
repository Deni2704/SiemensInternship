package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
public interface ItemService {
    List<Item> findAll();
    Optional<Item> findById(Long id);
    Item save(Item item);
    void deleteById(Long id);
    CompletableFuture<List<Item>> processItemsAsync();
}

