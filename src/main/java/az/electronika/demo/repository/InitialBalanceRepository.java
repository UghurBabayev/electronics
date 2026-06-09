package az.electronika.demo.repository;

import az.electronika.demo.entity.InitialBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InitialBalanceRepository extends JpaRepository<InitialBalance, Long> {

    List<InitialBalance> findByCreatedByUsername(String username);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM InitialBalance b")
    BigDecimal sumAllBalances();

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM InitialBalance b WHERE b.createdBy.username = :username")
    BigDecimal sumBalancesByUser(@Param("username") String username);
}