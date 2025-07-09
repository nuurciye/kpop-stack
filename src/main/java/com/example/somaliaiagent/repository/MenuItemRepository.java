package com.example.somaliaiagent.repository;

import com.example.somaliaiagent.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByName(String name);

    List<MenuItem> findByCategory(String category);

    List<MenuItem> findByIsAvailableTrue();

    List<MenuItem> findByNameContainingIgnoreCase(String keyword);

    List<MenuItem> findByNameSomaliContainingIgnoreCase(String keyword);

    // Query for items by name in either English or Somali
    // Note: For more complex multi-lingual search, consider full-text search capabilities
    // or separate queries combined in the service layer.
    // Example:
    // @Query("SELECT m FROM MenuItem m WHERE lower(m.name) LIKE lower(concat('%', :keyword, '%')) OR lower(m.nameSomali) LIKE lower(concat('%', :keyword, '%'))")
    // List<MenuItem> findByNameInEnglishOrSomali(@Param("keyword") String keyword);
}
