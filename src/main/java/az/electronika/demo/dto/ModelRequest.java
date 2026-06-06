package az.electronika.demo.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ModelRequest(
        @NotBlank String name,
        Long brandId,
        Long categoryId,
        BigDecimal salePrice
) {}