package az.electronika.demo.service;

import az.electronika.demo.dto.InitialBalanceRequest;
import az.electronika.demo.entity.InitialBalance;
import az.electronika.demo.entity.User;
import az.electronika.demo.repository.InitialBalanceRepository;
import az.electronika.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InitialBalanceService {

    private final InitialBalanceRepository repo;
    private final UserRepository userRepo;

    public List<InitialBalance> getAll() {
        return repo.findAll();
    }

    public BigDecimal getTotal() {
        return repo.sumAllBalances();
    }

    public InitialBalance create(InitialBalanceRequest req) {
        User user = currentUser();
        InitialBalance balance = InitialBalance.builder()
                .amount(req.amount())
                .balanceDate(req.balanceDate())
                .note(req.note())
                .createdBy(user)
                .build();
        return repo.save(balance);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElse(null);
    }
}