package az.electronika.demo.dto;

import az.electronika.demo.entity.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String fullName,
        String phone,
        String address,
        String note,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getFullName(), c.getPhone(),
                c.getAddress(), c.getNote(), c.getCreatedAt());
    }
}