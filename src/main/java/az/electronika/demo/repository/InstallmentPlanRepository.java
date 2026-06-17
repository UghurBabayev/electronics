package az.electronika.demo.repository;

import az.electronika.demo.entity.InstallmentPlan;
import az.electronika.demo.entity.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InstallmentPlanRepository extends JpaRepository<InstallmentPlan, Long> {

    Optional<InstallmentPlan> findBySaleId(Long saleId);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findByStatus(InstallmentStatus status);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findByStatusAndSaleCreatedByUsername(InstallmentStatus status, String username);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findAll();

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findBySaleCreatedByUsername(String username);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findBySaleCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    List<InstallmentPlan> findBySaleCustomerIdAndSaleCreatedByUsername(Long customerId, String username);

    @EntityGraph(attributePaths = {"sale", "sale.product", "sale.product.model", "sale.customer", "payments"})
    Optional<InstallmentPlan> findById(Long id);

    @Query("SELECT COALESCE(SUM(p.totalAmount - p.paidAmount), 0) FROM InstallmentPlan p WHERE p.status = 'ACTIVE'")
    BigDecimal totalOutstandingDebt();

    @Query("SELECT COALESCE(SUM(p.totalAmount - p.paidAmount), 0) FROM InstallmentPlan p WHERE p.status = 'ACTIVE' AND p.sale.createdBy.username = :username")
    BigDecimal totalOutstandingDebtByUser(@Param("username") String username);

    @Query("SELECT COUNT(p) FROM InstallmentPlan p WHERE p.status = 'OVERDUE'")
    long countOverdue();

    @Query("SELECT COUNT(p) FROM InstallmentPlan p WHERE p.status = 'OVERDUE' AND p.sale.createdBy.username = :username")
    long countOverdueByUser(@Param("username") String username);
}