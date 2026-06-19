package az.electronika.demo.dto;

import az.electronika.demo.entity.CashEntry;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashEntryResponse(
        Long id,
        LocalDate entryDate,
        BigDecimal amount,
        String note
) {
    public static CashEntryResponse from(CashEntry e) {
        return new CashEntryResponse(e.getId(), e.getEntryDate(), e.getAmount(), e.getNote());
    }
}