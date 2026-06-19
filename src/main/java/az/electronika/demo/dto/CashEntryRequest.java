package az.electronika.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashEntryRequest(
        LocalDate entryDate,
        BigDecimal amount,
        String note
) {}