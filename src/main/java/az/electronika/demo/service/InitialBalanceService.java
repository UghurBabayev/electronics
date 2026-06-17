package az.electronika.demo.service;

import az.electronika.demo.dto.InitialBalanceRequest;
import az.electronika.demo.dto.InitialBalanceResponse;
import az.electronika.demo.entity.InitialBalance;
import az.electronika.demo.repository.InitialBalanceRepository;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InitialBalanceService {

    private final InitialBalanceRepository repo;
    private final SecurityHelper security;

    public List<InitialBalanceResponse> getAll() {
        List<InitialBalance> list = security.isAdmin()
                ? repo.findAll()
                : repo.findByCreatedByUsername(security.currentUsername());
        return list.stream().map(InitialBalanceResponse::from).toList();
    }

    public BigDecimal getTotal() {
        return security.isAdmin()
                ? repo.sumAllBalances()
                : repo.sumBalancesByUser(security.currentUsername());
    }

    public InitialBalanceResponse create(InitialBalanceRequest req) {
        InitialBalance balance = InitialBalance.builder()
                .amount(req.amount())
                .balanceDate(req.balanceDate())
                .note(req.note())
                .createdBy(security.currentUser())
                .build();
        Long id = repo.save(balance).getId();
        return InitialBalanceResponse.from(repo.findById(id).orElseThrow());
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}