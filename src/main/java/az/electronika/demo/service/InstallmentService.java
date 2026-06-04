package az.electronika.demo.service;

import az.electronika.demo.dto.InstallmentPlanResponse;
import az.electronika.demo.entity.InstallmentPayment;
import az.electronika.demo.entity.InstallmentPlan;
import az.electronika.demo.entity.enums.InstallmentStatus;
import az.electronika.demo.repository.InstallmentPaymentRepository;
import az.electronika.demo.repository.InstallmentPlanRepository;
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

    public List<InstallmentPlanResponse> getAllActive() {
        return planRepo.findByStatus(InstallmentStatus.ACTIVE)
                .stream().map(InstallmentPlanResponse::from).toList();
    }

    public List<InstallmentPlanResponse> getAll() {
        return planRepo.findAll().stream().map(InstallmentPlanResponse::from).toList();
    }

    public List<InstallmentPlanResponse> getByCustomer(Long customerId) {
        return planRepo.findBySaleCustomerId(customerId)
                .stream().map(InstallmentPlanResponse::from).toList();
    }

    public InstallmentPlanResponse getById(Long id) {
        return InstallmentPlanResponse.from(findPlanOrThrow(id));
    }

    public List<InstallmentPlanResponse> getOverdue() {
        List<InstallmentPayment> overduePayments =
                paymentRepo.findByIsPaidFalseAndDueDateBefore(LocalDate.now());
        return overduePayments.stream()
                .map(p -> p.getPlan().getId())
                .distinct()
                .map(this::findPlanOrThrow)
                .map(InstallmentPlanResponse::from)
                .toList();
    }

    @Transactional
    public InstallmentPlanResponse markPaymentPaid(Long paymentId) {
        InstallmentPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödəniş tapılmadı: " + paymentId));

        if (payment.isPaid()) {
            throw new RuntimeException("Bu ödəniş artıq ödənilib");
        }

        payment.setPaid(true);
        payment.setPaidDate(LocalDate.now());
        paymentRepo.save(payment);

        InstallmentPlan plan = payment.getPlan();
        plan.setPaidAmount(plan.getPaidAmount().add(payment.getAmount()));

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