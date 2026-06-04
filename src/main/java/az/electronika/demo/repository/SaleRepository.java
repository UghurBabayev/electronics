package az.electronika.demo.repository;

import az.electronika.demo.entity.Sale;
import az.electronika.demo.entity.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCustomerId(Long customerId);
    List<Sale> findByPaymentType(PaymentType paymentType);
    List<Sale> findBySaleDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(s.salePrice * s.quantity), 0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(p.purchasePrice * s.quantity), 0) FROM Sale s JOIN s.product p WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumCostBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}