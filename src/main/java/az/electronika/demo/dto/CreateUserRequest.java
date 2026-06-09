package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank String fullName,
        @NotBlank @Size(min = 4) String password,
        @NotBlank String role
) {}