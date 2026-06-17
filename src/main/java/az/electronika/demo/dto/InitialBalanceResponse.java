package az.electronika.demo.dto;

import az.electronika.demo.entity.InitialBalance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InitialBalanceResponse(
        Long id,
        BigDecimal amount,
        LocalDate balanceDate,
        String note,
        LocalDateTime createdAt
) {
    public static InitialBalanceResponse from(InitialBalance b) {
        return new InitialBalanceResponse(
                b.getId(), b.getAmount(), b.getBalanceDate(), b.getNote(), b.getCreatedAt()
        );
    }
}