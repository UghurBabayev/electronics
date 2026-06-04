package az.electronika.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InitialBalanceRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate balanceDate,
        String note
) {}