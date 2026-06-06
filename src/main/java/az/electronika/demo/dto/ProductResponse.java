package az.electronika.demo.dto;

import az.electronika.demo.entity.Model;
import az.electronika.demo.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long modelId,
        String modelName,
        String brand,
        Long brandId,
        String category,
        Long categoryId,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        LocalDate purchaseDate,
        Integer quantity,
        String description,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product p) {
        Model m = p.getModel();
        return new ProductResponse(
                p.getId(),
                m != null ? m.getId() : null,
                m != null ? m.getName() : null,
                m != null && m.getBrand() != null ? m.getBrand().getName() : null,
                m != null && m.getBrand() != null ? m.getBrand().getId() : null,
                m != null && m.getCategory() != null ? m.getCategory().getName() : null,
                m != null && m.getCategory() != null ? m.getCategory().getId() : null,
                p.getPurchasePrice(),
                m != null ? m.getSalePrice() : null,
                p.getPurchaseDate(),
                p.getQuantity(),
                p.getDescription(),
                p.getCreatedAt()
        );
    }
}