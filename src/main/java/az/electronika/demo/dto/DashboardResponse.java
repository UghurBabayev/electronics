package az.electronika.demo.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long todaySaleCount,
        BigDecimal todaySaleAmount,
        BigDecimal totalDebt,
        long inStockCount,
        long overdueCount,
        long expiredUserCount,
        long expiringSoonUserCount
) {}