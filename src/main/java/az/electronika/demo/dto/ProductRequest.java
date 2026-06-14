package az.electronika.demo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequest(
        @NotNull Long modelId,
        @NotNull @Positive BigDecimal purchasePrice,
        @NotNull LocalDate purchaseDate,
        @NotNull @Min(1) Integer quantity,
        BigDecimal salePrice,
        String description
) {}