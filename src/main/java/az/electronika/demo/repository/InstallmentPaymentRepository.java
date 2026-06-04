package az.electronika.demo.repository;

import az.electronika.demo.entity.InstallmentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InstallmentPaymentRepository extends JpaRepository<InstallmentPayment, Long> {
    List<InstallmentPayment> findByPlanId(Long planId);
    List<InstallmentPayment> findByIsPaidFalseAndDueDateBefore(LocalDate date);
    List<InstallmentPayment> findByIsPaidFalse();
}