package az.electronika.demo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequest(
        @NotBlank String name,
        Long categoryId,
        Long brandId,
        @NotNull @Positive BigDecimal purchasePrice,
        BigDecimal salePrice,
        @NotNull LocalDate purchaseDate,
        @NotNull @Min(1) Integer quantity,
        String description
) {}