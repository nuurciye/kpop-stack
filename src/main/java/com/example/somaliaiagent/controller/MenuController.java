package com.example.somaliaiagent.controller;

import com.example.somaliaiagent.model.MenuItem;
import com.example.somaliaiagent.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // GET /api/menu: Get all menu items
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems(
            @RequestParam(required = false) String available // "true" or "false"
    ) {
        List<MenuItem> menuItems;
        if ("true".equalsIgnoreCase(available)) {
            menuItems = menuService.getAvailableMenuItems();
        } else {
            // TODO: Consider if a "false" parameter should show unavailable or all.
            // For now, default to all if 'available' is not 'true'.
            menuItems = menuService.getAllMenuItems();
        }
        return ResponseEntity.ok(menuItems);
    }

    // GET /api/menu/{id}: Get a specific menu item by ID
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        Optional<MenuItem> menuItem = menuService.getMenuItemById(id);
        return menuItem.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/menu: Add a new menu item
    @PostMapping
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody MenuItem menuItem) {
        // TODO: Add input validation (e.g., using @Valid and DTOs)
        try {
            MenuItem newMenuItem = menuService.addMenuItem(menuItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMenuItem);
        } catch (Exception e) { // Catch specific exceptions like DataIntegrityViolationException
            // Log the exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Or more specific error
        }
    }

    // PUT /api/menu/{id}: Update an existing menu item
    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItemDetails) {
        // TODO: Add input validation
        Optional<MenuItem> updatedMenuItem = menuService.updateMenuItem(id, menuItemDetails);
        return updatedMenuItem.map(ResponseEntity::ok)
                              .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PATCH /api/menu/{id}/availability: Update a menu item's availability
    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItem> updateMenuItemAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Optional<MenuItem> updatedMenuItem = menuService.setItemAvailability(id, available);
        return updatedMenuItem.map(ResponseEntity::ok)
                              .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/menu/{id}: Delete a menu item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        boolean deleted = menuService.deleteMenuItem(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/menu/search?keyword=...&lang=...
    @GetMapping("/search")
    public ResponseEntity<List<MenuItem>> searchMenuItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "en") String lang) {
        List<MenuItem> items = menuService.findMenuItemsByKeyword(keyword, lang);
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(items);
    }
}
