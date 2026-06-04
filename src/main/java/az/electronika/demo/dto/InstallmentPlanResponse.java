package az.electronika.demo.dto;

import az.electronika.demo.entity.InstallmentPlan;
import az.electronika.demo.entity.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InstallmentPlanResponse(
        Long id,
        Long saleId,
        String customerName,
        String productName,
        BigDecimal totalAmount,
        BigDecimal monthlyPayment,
        Integer durationMonths,
        LocalDate startDate,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InstallmentStatus status,
        List<InstallmentPaymentResponse> payments
) {
    public static InstallmentPlanResponse from(InstallmentPlan plan) {
        BigDecimal remaining = plan.getTotalAmount().subtract(plan.getPaidAmount());
        List<InstallmentPaymentResponse> payments = plan.getPayments().stream()
                .map(InstallmentPaymentResponse::from)
                .toList();

        String customerName = plan.getSale().getCustomer() != null
                ? plan.getSale().getCustomer().getFullName() : null;

        return new InstallmentPlanResponse(
                plan.getId(),
                plan.getSale().getId(),
                customerName,
                plan.getSale().getProduct().getName(),
                plan.getTotalAmount(),
                plan.getMonthlyPayment(),
                plan.getDurationMonths(),
                plan.getStartDate(),
                plan.getPaidAmount(),
                remaining,
                plan.getStatus(),
                payments
        );
    }
}