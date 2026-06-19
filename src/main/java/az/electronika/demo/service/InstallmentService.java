package az.electronika.demo.service;

import az.electronika.demo.dto.InstallmentPlanResponse;
import az.electronika.demo.entity.InstallmentPayment;
import az.electronika.demo.entity.InstallmentPlan;
import az.electronika.demo.entity.enums.InstallmentStatus;
import az.electronika.demo.repository.InstallmentPaymentRepository;
import az.electronika.demo.repository.InstallmentPlanRepository;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final InstallmentPlanRepository planRepo;
    private final InstallmentPaymentRepository paymentRepo;
    private final SecurityHelper security;

    public List<InstallmentPlanResponse> getAllActive() {
        List<InstallmentPlan> list = security.isAdmin()
                ? planRepo.findByStatus(InstallmentStatus.ACTIVE)
                : planRepo.findByStatusAndSaleCreatedByUsername(InstallmentStatus.ACTIVE, security.currentUsername());
        return list.stream().map(InstallmentPlanResponse::from).toList();
    }

    public List<InstallmentPlanResponse> getAll() {
        List<InstallmentPlan> list = security.isAdmin()
                ? planRepo.findAll()
                : planRepo.findBySaleCreatedByUsername(security.currentUsername());
        return list.stream().map(InstallmentPlanResponse::from).toList();
    }

    public List<InstallmentPlanResponse> getByCustomer(Long customerId) {
        List<InstallmentPlan> list = security.isAdmin()
                ? planRepo.findBySaleCustomerId(customerId)
                : planRepo.findBySaleCustomerIdAndSaleCreatedByUsername(customerId, security.currentUsername());
        return list.stream().map(InstallmentPlanResponse::from).toList();
    }

    public InstallmentPlanResponse getById(Long id) {
        return InstallmentPlanResponse.from(findPlanOrThrow(id));
    }

    public List<InstallmentPlanResponse> getOverdue() {
        List<InstallmentPayment> overduePayments =
                paymentRepo.findByIsPaidFalseAndDueDateBefore(LocalDate.now());
        String username = security.currentUsername();
        boolean admin = security.isAdmin();
        return overduePayments.stream()
                .map(p -> p.getPlan().getId())
                .distinct()
                .map(this::findPlanOrThrow)
                .filter(plan -> admin || plan.getSale().getCreatedBy() != null
                        && plan.getSale().getCreatedBy().getUsername().equals(username))
                .map(InstallmentPlanResponse::from)
                .toList();
    }

    @Transactional
    public InstallmentPlanResponse markPaymentPaid(Long paymentId, java.math.BigDecimal amount) {
        InstallmentPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödəniş tapılmadı: " + paymentId));

        if (payment.isPaid()) {
            throw new RuntimeException("Bu ödəniş artıq ödənilib");
        }

        java.math.BigDecimal remaining = payment.getAmount().subtract(payment.getPaidAmount());
        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Məbləğ sıfırdan böyük olmalıdır");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new RuntimeException("Ödəniş məbləği qalıqdan böyük ola bilməz: " + remaining + " ₼");
        }

        payment.setPaidAmount(payment.getPaidAmount().add(amount));

        if (payment.getPaidAmount().compareTo(payment.getAmount()) >= 0) {
            payment.setPaid(true);
            payment.setPaidDate(LocalDate.now());
        }
        paymentRepo.save(payment);

        InstallmentPlan plan = payment.getPlan();
        plan.setPaidAmount(plan.getPaidAmount().add(amount));

        boolean allPaid = plan.getPayments().stream().allMatch(InstallmentPayment::isPaid);
        if (allPaid) {
            plan.setStatus(InstallmentStatus.COMPLETED);
        }

        return InstallmentPlanResponse.from(planRepo.save(plan));
    }

    private InstallmentPlan findPlanOrThrow(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Nisiyə planı tapılmadı: " + id));
    }
}