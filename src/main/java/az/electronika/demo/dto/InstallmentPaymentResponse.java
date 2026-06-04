package az.electronika.demo.dto;

import az.electronika.demo.entity.InstallmentPayment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentPaymentResponse(
        Long id,
        LocalDate dueDate,
        BigDecimal amount,
        LocalDate paidDate,
        boolean isPaid
) {
    public static InstallmentPaymentResponse from(InstallmentPayment p) {
        return new InstallmentPaymentResponse(
                p.getId(), p.getDueDate(), p.getAmount(), p.getPaidDate(), p.isPaid()
        );
    }
}