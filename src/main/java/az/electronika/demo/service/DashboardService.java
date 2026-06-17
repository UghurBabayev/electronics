package az.electronika.demo.service;

import az.electronika.demo.dto.DashboardResponse;
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
public class DashboardService {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final InstallmentPlanRepository planRepo;
    private final SecurityHelper security;

    public DashboardResponse get() {
        boolean admin = security.isAdmin();
        String username = security.currentUsername();
        LocalDate today = LocalDate.now();

        long todaySaleCount = admin
                ? saleRepo.countByDate(today)
                : saleRepo.countByDateAndUser(today, username);

        BigDecimal todaySaleAmount = admin
                ? saleRepo.sumRevenueBetween(today, today)
                : saleRepo.sumRevenueBetweenByUser(username, today, today);

        BigDecimal totalDebt = admin
                ? planRepo.totalOutstandingDebt()
                : planRepo.totalOutstandingDebtByUser(username);

        long inStockCount = admin
                ? productRepo.countInStock()
                : productRepo.countInStockByUser(username);

        long overdueCount = admin
                ? planRepo.countOverdue()
                : planRepo.countOverdueByUser(username);

        return new DashboardResponse(todaySaleCount, todaySaleAmount, totalDebt, inStockCount, overdueCount);
    }
}