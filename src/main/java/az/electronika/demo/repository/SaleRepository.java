package az.electronika.demo.repository;

import az.electronika.demo.entity.Sale;
import az.electronika.demo.entity.enums.PaymentType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    List<Sale> findByPaymentType(PaymentType paymentType);

    @EntityGraph(attributePaths = {"product", "product.model", "customer", "createdBy"})
    List<Sale> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"product", "product.model", "customer", "createdBy"})
    List<Sale> findByCustomerIdAndCreatedByUsername(Long customerId, String username);

    @Query("SELECT COALESCE(SUM(s.salePrice * s.quantity), 0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.salePrice * s.quantity), 0) FROM Sale s WHERE s.createdBy.username = :username AND s.saleDate BETWEEN :from AND :to")
    BigDecimal sumRevenueBetweenByUser(@Param("username") String username, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(p.purchasePrice * s.quantity), 0) FROM Sale s JOIN s.product p WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumCostBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(p.purchasePrice * s.quantity), 0) FROM Sale s JOIN s.product p WHERE s.createdBy.username = :username AND s.saleDate BETWEEN :from AND :to")
    BigDecimal sumCostBetweenByUser(@Param("username") String username, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate = :date AND s.createdBy.username = :username")
    long countByDateAndUser(@Param("date") LocalDate date, @Param("username") String username);
}