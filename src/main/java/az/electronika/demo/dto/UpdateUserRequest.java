package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String fullName,
        @NotBlank String role,
        boolean active,
        String newPassword
) {}