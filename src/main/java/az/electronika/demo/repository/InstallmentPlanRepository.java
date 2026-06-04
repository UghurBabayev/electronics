package az.electronika.demo.repository;

import az.electronika.demo.entity.InstallmentPlan;
import az.electronika.demo.entity.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InstallmentPlanRepository extends JpaRepository<InstallmentPlan, Long> {
    Optional<InstallmentPlan> findBySaleId(Long saleId);
    List<InstallmentPlan> findByStatus(InstallmentStatus status);
    List<InstallmentPlan> findBySaleCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(p.totalAmount - p.paidAmount), 0) FROM InstallmentPlan p WHERE p.status = 'ACTIVE'")
    BigDecimal totalOutstandingDebt();
}