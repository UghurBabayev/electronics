package az.electronika.demo.service;

import az.electronika.demo.dto.CustomerRequest;
import az.electronika.demo.dto.CustomerResponse;
import az.electronika.demo.entity.Customer;
import az.electronika.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repo;

    public List<CustomerResponse> getAll() {
        return repo.findAll().stream().map(CustomerResponse::from).toList();
    }

    public List<CustomerResponse> search(String name) {
        return repo.findByFullNameContainingIgnoreCase(name)
                .stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse getById(Long id) {
        return CustomerResponse.from(findOrThrow(id));
    }

    public CustomerResponse create(CustomerRequest req) {
        Customer c = Customer.builder()
                .fullName(req.fullName())
                .phone(req.phone())
                .address(req.address())
                .note(req.note())
                .build();
        return CustomerResponse.from(repo.save(c));
    }

    public CustomerResponse update(Long id, CustomerRequest req) {
        Customer c = findOrThrow(id);
        c.setFullName(req.fullName());
        c.setPhone(req.phone());
        c.setAddress(req.address());
        c.setNote(req.note());
        return CustomerResponse.from(repo.save(c));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private Customer findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Müştəri tapılmadı: " + id));
    }
}