package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ModelRequest(
        @NotBlank String name,
        Long brandId,
        Long categoryId
) {}