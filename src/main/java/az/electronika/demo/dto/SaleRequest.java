package az.electronika.demo.dto;

import az.electronika.demo.entity.enums.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaleRequest(
        @NotNull Long productId,
        Long customerId,
        @NotNull @Positive BigDecimal salePrice,
        @NotNull LocalDate saleDate,
        @NotNull PaymentType paymentType,
        @NotNull @Min(1) Integer quantity,
        String note,

        // Yalnız paymentType=CREDIT olduqda tələb olunur
        BigDecimal monthlyPayment,
        Integer durationMonths
) {}