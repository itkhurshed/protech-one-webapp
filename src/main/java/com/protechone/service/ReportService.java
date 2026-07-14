package com.protechone.service;

import com.protechone.repository.*;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Read-only, filterable reports layer sitting on top of the transactional
 * repositories: sales, purchases, inventory valuation/low-stock, expenses,
 * and receivable/payable aging. Every module in the spec calls for date,
 * branch and status filters plus export — export (PDF/Excel/CSV) is handled
 * client-side against these JSON payloads for Phase 1.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public Map<String, Object> salesReport(LocalDate start, LocalDate end) {
        Long companyId = currentUser.companyId();
        LocalDate s = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate e = end != null ? end : LocalDate.now();
        return Map.of(
                "periodStart", s,
                "periodEnd", e,
                "totalSales", salesInvoiceRepository.totalSalesBetween(companyId, s, e),
                "outstandingReceivables", salesInvoiceRepository.totalOutstanding(companyId)
        );
    }

    public Map<String, Object> purchaseReport(LocalDate start, LocalDate end) {
        Long companyId = currentUser.companyId();
        LocalDate s = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate e = end != null ? end : LocalDate.now();
        return Map.of(
                "periodStart", s,
                "periodEnd", e,
                "totalPurchases", purchaseInvoiceRepository.totalPurchasesBetween(companyId, s, e),
                "outstandingPayables", purchaseInvoiceRepository.totalOutstandingPayable(companyId)
        );
    }

    public Map<String, Object> expenseReport(LocalDate start, LocalDate end) {
        Long companyId = currentUser.companyId();
        LocalDate s = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate e = end != null ? end : LocalDate.now();
        return Map.of(
                "periodStart", s,
                "periodEnd", e,
                "totalExpenses", expenseRepository.totalExpensesBetween(companyId, s, e)
        );
    }

    public Map<String, Object> inventoryReport() {
        Long companyId = currentUser.companyId();
        return Map.of(
                "totalProducts", productRepository.countByCompanyId(companyId),
                "totalInventoryValue", stockLevelRepository.totalInventoryValue(companyId),
                "lowStockCount", stockLevelRepository.findLowStock(companyId).size()
        );
    }
}
