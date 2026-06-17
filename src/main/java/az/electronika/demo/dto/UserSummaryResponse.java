package az.electronika.demo.dto;

import az.electronika.demo.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String username,
        String fullName,
        String role,
        boolean active,
        LocalDate accessUntil,
        LocalDateTime createdAt
) {
    public static UserSummaryResponse from(User u) {
        return new UserSummaryResponse(
                u.getId(), u.getUsername(), u.getFullName(),
                u.getRole().name(), u.isActive(), u.getAccessUntil(), u.getCreatedAt()
        );
    }
}