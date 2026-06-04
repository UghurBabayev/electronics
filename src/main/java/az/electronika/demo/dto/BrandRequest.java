package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(@NotBlank String name) {}