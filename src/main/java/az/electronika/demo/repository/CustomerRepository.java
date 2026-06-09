package az.electronika.demo.repository;

import az.electronika.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByFullNameContainingIgnoreCase(String name);
    List<Customer> findByCreatedByUsername(String username);
    List<Customer> findByCreatedByUsernameAndFullNameContainingIgnoreCase(String username, String name);
}