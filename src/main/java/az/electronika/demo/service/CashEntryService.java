package az.electronika.demo.service;

import az.electronika.demo.dto.CashEntryRequest;
import az.electronika.demo.dto.CashEntryResponse;
import az.electronika.demo.entity.CashEntry;
import az.electronika.demo.repository.CashEntryRepository;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CashEntryService {

    private final CashEntryRepository repo;
    private final SecurityHelper security;

    public Map<String, Object> getAll() {
        List<CashEntry> list = security.isAdmin()
                ? repo.findAllByOrderByEntryDateDescIdDesc()
                : repo.findByCreatedByUsernameOrderByEntryDateDescIdDesc(security.currentUsername());

        List<CashEntryResponse> entries = list.stream().map(CashEntryResponse::from).toList();
        BigDecimal total = list.stream()
                .map(CashEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of("entries", entries, "total", total);
    }

    public CashEntryResponse create(CashEntryRequest req) {
        if (req.entryDate() == null) throw new RuntimeException("Tarix mütləqdir");
        if (req.amount() == null) throw new RuntimeException("Məbləğ mütləqdir");
        CashEntry entry = CashEntry.builder()
                .entryDate(req.entryDate())
                .amount(req.amount())
                .note(req.note())
                .createdBy(security.currentUser())
                .build();
        return CashEntryResponse.from(repo.save(entry));
    }

    public void delete(Long id) {
        CashEntry entry = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Qeyd tapılmadı"));
        if (!security.isAdmin() &&
            (entry.getCreatedBy() == null ||
             !entry.getCreatedBy().getUsername().equals(security.currentUsername()))) {
            throw new RuntimeException("Bu qeydi silmək icazəniz yoxdur");
        }
        repo.deleteById(id);
    }
}