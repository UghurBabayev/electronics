package az.electronika.demo.repository;

import az.electronika.demo.entity.CashEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashEntryRepository extends JpaRepository<CashEntry, Long> {
    List<CashEntry> findAllByOrderByEntryDateDescIdDesc();
    List<CashEntry> findByCreatedByUsernameOrderByEntryDateDescIdDesc(String username);
}