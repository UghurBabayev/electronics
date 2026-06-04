package az.electronika.demo.repository;

import az.electronika.demo.entity.InitialBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface InitialBalanceRepository extends JpaRepository<InitialBalance, Long> {

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM InitialBalance b")
    BigDecimal sumAllBalances();
}