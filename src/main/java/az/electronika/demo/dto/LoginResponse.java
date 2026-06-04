package az.electronika.demo.dto;

public record LoginResponse(
        String token,
        String username,
        String fullName,
        String role
) {}