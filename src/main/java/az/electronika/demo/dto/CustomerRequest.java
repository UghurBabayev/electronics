package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank String fullName,
        String phone,
        String address,
        String note
) {}