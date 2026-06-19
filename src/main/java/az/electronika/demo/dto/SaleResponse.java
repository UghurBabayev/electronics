package az.electronika.demo.dto;

import az.electronika.demo.entity.Sale;
import az.electronika.demo.entity.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SaleResponse(
        Long id,
        Long productId,
        String productName,
        String brandName,
        Long customerId,
        String customerName,
        BigDecimal salePrice,
        LocalDate saleDate,
        PaymentType paymentType,
        Integer quantity,
        String note,
        LocalDateTime createdAt
) {
    public static SaleResponse from(Sale s) {
        var model = s.getProduct().getModel();
        return new SaleResponse(
                s.getId(),
                s.getProduct().getId(),
                model != null ? model.getName() : null,
                model != null && model.getBrand() != null ? model.getBrand().getName() : null,
                s.getCustomer() != null ? s.getCustomer().getId() : null,
                s.getCustomer() != null ? s.getCustomer().getFullName() : null,
                s.getSalePrice(), s.getSaleDate(), s.getPaymentType(),
                s.getQuantity(), s.getNote(), s.getCreatedAt()
        );
    }
}