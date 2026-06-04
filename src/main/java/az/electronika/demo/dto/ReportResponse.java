package az.electronika.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal profit,
        BigDecimal totalDebt,
        BigDecimal inventoryValue,
        BigDecimal initialBalance,
        BigDecimal cashOnHand
) {}