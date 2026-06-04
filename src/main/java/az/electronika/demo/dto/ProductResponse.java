package az.electronika.demo.dto;

import az.electronika.demo.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String category,
        Long categoryId,
        String brand,
        Long brandId,
        BigDecimal purchasePrice,
        LocalDate purchaseDate,
        Integer quantity,
        String description,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getBrand() != null ? p.getBrand().getName() : null,
                p.getBrand() != null ? p.getBrand().getId() : null,
                p.getPurchasePrice(), p.getPurchaseDate(),
                p.getQuantity(), p.getDescription(), p.getCreatedAt()
        );
    }
}