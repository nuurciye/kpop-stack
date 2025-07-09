package com.example.somaliaiagent.service;

import com.example.somaliaiagent.model.MenuItem;
import com.example.somaliaiagent.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    @Autowired
    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        // TODO: Add pagination and sorting options
        return menuItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableMenuItems() {
        return menuItemRepository.findByIsAvailableTrue();
    }

    @Transactional(readOnly = true)
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<MenuItem> getMenuItemByName(String name) {
        return menuItemRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> findMenuItemsByKeyword(String keyword, String language) {
        if ("so".equalsIgnoreCase(language)) {
            return menuItemRepository.findByNameSomaliContainingIgnoreCase(keyword);
        }
        return menuItemRepository.findByNameContainingIgnoreCase(keyword);
        // For combined search, you might use a custom query or combine results
        // return menuItemRepository.findByNameInEnglishOrSomali(keyword);
    }

    @Transactional
    public MenuItem addMenuItem(MenuItem menuItem) {
        // TODO: Add validation (e.g., ensure name is unique)
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public Optional<MenuItem> updateMenuItem(Long id, MenuItem menuItemDetails) {
        return menuItemRepository.findById(id)
                .map(existingItem -> {
                    if (menuItemDetails.getName() != null) {
                        existingItem.setName(menuItemDetails.getName());
                    }
                    if (menuItemDetails.getNameSomali() != null) {
                        existingItem.setNameSomali(menuItemDetails.getNameSomali());
                    }
                    if (menuItemDetails.getDescription() != null) {
                        existingItem.setDescription(menuItemDetails.getDescription());
                    }
                    if (menuItemDetails.getDescriptionSomali() != null) {
                        existingItem.setDescriptionSomali(menuItemDetails.getDescriptionSomali());
                    }
                    if (menuItemDetails.getPrice() != null) {
                        existingItem.setPrice(menuItemDetails.getPrice());
                    }
                    if (menuItemDetails.getCategory() != null) {
                        existingItem.setCategory(menuItemDetails.getCategory());
                    }
                    existingItem.setAvailable(menuItemDetails.isAvailable()); // isAvailable is boolean primitive

                    if (menuItemDetails.getImageUrl() != null) {
                        existingItem.setImageUrl(menuItemDetails.getImageUrl());
                    }
                    return menuItemRepository.save(existingItem);
                });
    }

    @Transactional
    public boolean deleteMenuItem(Long id) {
        if (menuItemRepository.existsById(id)) {
            // TODO: Consider implications: what if this item is in past orders?
            // Soft delete might be better (e.g., set isAvailable = false and isDeleted = true)
            menuItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<MenuItem> setItemAvailability(Long id, boolean isAvailable) {
        return menuItemRepository.findById(id)
                .map(item -> {
                    item.setAvailable(isAvailable);
                    return menuItemRepository.save(item);
                });
    }
}
