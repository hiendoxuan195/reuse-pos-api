package com.example.reusepos.controller;

import com.example.reusepos.model.BuybackItem;
import com.example.reusepos.service.BuybackItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class BuybackItemController {

    private final BuybackItemService service;

    public BuybackItemController(BuybackItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<BuybackItem> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        if (status != null) return service.findByStatus(status);
        if (categoryId != null) return service.findByCategoryId(categoryId);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuybackItem> get(@PathVariable Long id) {
        BuybackItem item = service.findById(id);
        if (item == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(item);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuybackItem create(@RequestBody BuybackItem item) {
        return service.create(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuybackItem> update(@PathVariable Long id, @RequestBody BuybackItem item) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(service.update(id, item));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BuybackItem> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(service.updateStatus(id, body.get("status")));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "app", "reuse-pos-api");
    }
}
