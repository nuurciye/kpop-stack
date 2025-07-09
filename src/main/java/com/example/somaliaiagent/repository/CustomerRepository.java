package com.example.somaliaiagent.repository;

import com.example.somaliaiagent.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // Additional query methods can be defined here if needed
    // For example:
    // List<Customer> findByNameContainingIgnoreCase(String name);
}
