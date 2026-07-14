package com.protechone.service;

import com.protechone.dto.dashboard.ChartPointResponse;
import com.protechone.dto.dashboard.DashboardSummaryResponse;
import com.protechone.dto.dashboard.RecentTransactionResponse;
import com.protechone.repository.*;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates KPIs and chart series for the executive dashboard. Figures are
 * computed on read (fine for Phase 1 data volumes); a materialized summary
 * table would be the Phase 2+ optimization for larger datasets.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final CurrentUser currentUser;

    public DashboardSummaryResponse summary() {
        Long companyId = currentUser.companyId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        BigDecimal todaySales = salesInvoiceRepository.totalSalesForDate(companyId, today);
        BigDecimal monthlySales = salesInvoiceRepository.totalSalesBetween(companyId, monthStart, today);
        BigDecimal monthlyPurchases = purchaseInvoiceRepository.totalPurchasesBetween(companyId, monthStart, today);
        BigDecimal monthlyExpenses = expenseRepository.totalExpensesBetween(companyId, monthStart, today);
        BigDecimal grossProfit = monthlySales.subtract(monthlyPurchases);
        BigDecimal cashBalance = monthlySales.subtract(monthlyExpenses).subtract(monthlyPurchases);

        return new DashboardSummaryResponse(
                todaySales, monthlySales, grossProfit, monthlyExpenses, cashBalance,
                salesInvoiceRepository.totalOutstanding(companyId),
                purchaseInvoiceRepository.totalOutstandingPayable(companyId),
                customerRepository.countByCompanyId(companyId),
                supplierRepository.countByCompanyId(companyId),
                userRepository.countByCompanyId(companyId),
                productRepository.countByCompanyId(companyId),
                stockLevelRepository.findLowStock(companyId).size(),
                stockLevelRepository.totalInventoryValue(companyId)
        );
    }

    public List<ChartPointResponse> salesByMonth() {
        Long companyId = currentUser.companyId();
        LocalDate since = YearMonth.now().minusMonths(11).atDay(1);
        List<Object[]> rows = salesInvoiceRepository.monthlySalesSince(companyId, since);
        List<ChartPointResponse> points = new ArrayList<>();
        for (Object[] row : rows) {
            points.add(new ChartPointResponse((String) row[0], (BigDecimal) row[1]));
        }
        return points;
    }

    public List<ChartPointResponse> topSellingProducts() {
        Long companyId = currentUser.companyId();
        List<Object[]> rows = salesInvoiceItemRepository.topSellingProducts(companyId);
        List<ChartPointResponse> points = new ArrayList<>();
        int limit = Math.min(rows.size(), 8);
        for (int i = 0; i < limit; i++) {
            Object[] row = rows.get(i);
            points.add(new ChartPointResponse((String) row[1], (BigDecimal) row[3]));
        }
        return points;
    }

    public List<RecentTransactionResponse> recentTransactions() {
        Long companyId = currentUser.companyId();
        List<RecentTransactionResponse> result = new ArrayList<>();
        salesInvoiceRepository.findTop10ByCompanyIdOrderByCreatedAtDesc(companyId).forEach(i ->
                result.add(new RecentTransactionResponse("SALE", i.getInvoiceNumber(), i.getCustomer().getName(),
                        i.getGrandTotal(), i.getStatus(), i.getCreatedAt())));
        result.sort((a, b) -> b.date().compareTo(a.date()));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }
}
