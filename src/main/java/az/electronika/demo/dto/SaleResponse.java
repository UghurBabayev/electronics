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
        return new SaleResponse(
                s.getId(),
                s.getProduct().getId(),
                s.getProduct().getModel() != null ? s.getProduct().getModel().getName() : null,
                s.getCustomer() != null ? s.getCustomer().getId() : null,
                s.getCustomer() != null ? s.getCustomer().getFullName() : null,
                s.getSalePrice(), s.getSaleDate(), s.getPaymentType(),
                s.getQuantity(), s.getNote(), s.getCreatedAt()
        );
    }
}