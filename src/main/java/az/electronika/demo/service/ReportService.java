package az.electronika.demo.service;

import az.electronika.demo.dto.ReportResponse;
import az.electronika.demo.repository.InitialBalanceRepository;
import az.electronika.demo.repository.InstallmentPlanRepository;
import az.electronika.demo.repository.ProductRepository;
import az.electronika.demo.repository.SaleRepository;
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

    public ReportResponse generate(LocalDate from, LocalDate to) {
        BigDecimal revenue  = saleRepo.sumRevenueBetween(from, to);
        BigDecimal cost     = saleRepo.sumCostBetween(from, to);
        BigDecimal profit   = revenue.subtract(cost);
        BigDecimal debt     = planRepo.totalOutstandingDebt();
        BigDecimal inventory = inventoryValue();
        BigDecimal initial  = balanceRepo.sumAllBalances();
        BigDecimal cash     = initial.add(revenue).subtract(cost);

        return new ReportResponse(from, to, revenue, cost, profit, debt, inventory, initial, cash);
    }

    private BigDecimal inventoryValue() {
        return productRepo.findAll().stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}