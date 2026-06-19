package az.electronika.demo.service;

import az.electronika.demo.dto.ReportResponse;
import az.electronika.demo.repository.InitialBalanceRepository;
import az.electronika.demo.repository.InstallmentPlanRepository;
import az.electronika.demo.repository.ProductRepository;
import az.electronika.demo.repository.SaleRepository;
import az.electronika.demo.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepo;
    private final InstallmentPlanRepository planRepo;
    private final ProductRepository productRepo;
    private final InitialBalanceRepository balanceRepo;
    private final SecurityHelper security;

    public ReportResponse generate(LocalDate from, LocalDate to) {
        boolean admin = security.isAdmin();
        String username = security.currentUsername();

        BigDecimal revenue = admin
                ? saleRepo.sumRevenueBetween(from, to)
                : saleRepo.sumRevenueBetweenByUser(username, from, to);

        BigDecimal cost = admin
                ? saleRepo.sumCostBetween(from, to)
                : saleRepo.sumCostBetweenByUser(username, from, to);

        BigDecimal profit = revenue.subtract(cost);

        BigDecimal debt = admin
                ? planRepo.totalOutstandingDebt()
                : planRepo.totalOutstandingDebtByUser(username);

        BigDecimal inventory = inventoryValue(admin, username);

        BigDecimal initial = admin
                ? balanceRepo.sumAllBalances()
                : balanceRepo.sumBalancesByUser(username);

        BigDecimal cash = initial.add(revenue).subtract(cost);

        return new ReportResponse(from, to, revenue, cost, profit, debt, inventory, initial, cash);
    }

    private BigDecimal inventoryValue(boolean admin, String username) {
        var products = admin ? productRepo.findInStock() : productRepo.findInStockByUser(username);
        return products.stream()
                .map(p -> p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}