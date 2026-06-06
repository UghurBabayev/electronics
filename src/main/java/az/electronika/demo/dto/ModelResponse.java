package az.electronika.demo.dto;

import az.electronika.demo.entity.Model;
import java.math.BigDecimal;

public record ModelResponse(
        Long id,
        String name,
        String brand,
        Long brandId,
        String category,
        Long categoryId,
        BigDecimal salePrice
) {
    public static ModelResponse from(Model m) {
        return new ModelResponse(
                m.getId(), m.getName(),
                m.getBrand() != null ? m.getBrand().getName() : null,
                m.getBrand() != null ? m.getBrand().getId() : null,
                m.getCategory() != null ? m.getCategory().getName() : null,
                m.getCategory() != null ? m.getCategory().getId() : null,
                m.getSalePrice()
        );
    }
}