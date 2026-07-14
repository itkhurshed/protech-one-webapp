package com.protechone.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal todaySales,
        BigDecimal monthlySales,
        BigDecimal grossProfit,
        BigDecimal monthlyExpenses,
        BigDecimal cashBalance,
        BigDecimal outstandingReceivables,
        BigDecimal outstandingPayables,
        long customerCount,
        long supplierCount,
        long employeeCount,
        long productCount,
        long lowStockCount,
        BigDecimal inventoryValue
) {}
