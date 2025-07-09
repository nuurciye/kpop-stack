package com.example.somaliaiagent.service;

import com.example.somaliaiagent.model.Customer;
import com.example.somaliaiagent.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        // TODO: Add pagination
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        // TODO: Add validation (e.g., phone number format)
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer getOrCreateCustomer(String phoneNumber, String name, String preferredLanguage) {
        return customerRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(phoneNumber, name, preferredLanguage);
                    return customerRepository.save(newCustomer);
                });
    }

    @Transactional
    public Optional<Customer> updateCustomer(Long id, Customer customerDetails) {
        return customerRepository.findById(id)
                .map(existingCustomer -> {
                    if (customerDetails.getName() != null) {
                        existingCustomer.setName(customerDetails.getName());
                    }
                    if (customerDetails.getPhoneNumber() != null) {
                        // TODO: Add validation if phone number changes (e.g. uniqueness)
                        existingCustomer.setPhoneNumber(customerDetails.getPhoneNumber());
                    }
                    if (customerDetails.getPreferredLanguage() != null) {
                        existingCustomer.setPreferredLanguage(customerDetails.getPreferredLanguage());
                    }
                    return customerRepository.save(existingCustomer);
                });
    }

    @Transactional
    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Placeholder for more complex customer interaction logic
    public void recordInteraction(Customer customer, String interactionType) {
        // E.g., log interaction, update last interaction date, etc.
        System.out.println("Interaction recorded for customer " + customer.getId() + ": " + interactionType);
    }
}
