package az.electronika.demo.repository;

import az.electronika.demo.entity.InitialBalance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InitialBalanceRepository extends JpaRepository<InitialBalance, Long> {

    @Override
    @EntityGraph(attributePaths = {"createdBy"})
    List<InitialBalance> findAll();

    @EntityGraph(attributePaths = {"createdBy"})
    List<InitialBalance> findByCreatedByUsername(String username);

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<InitialBalance> findById(Long id);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM InitialBalance b")
    BigDecimal sumAllBalances();

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM InitialBalance b WHERE b.createdBy.username = :username")
    BigDecimal sumBalancesByUser(@Param("username") String username);
}