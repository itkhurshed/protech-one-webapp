package com.protechone.controller;

import com.protechone.service.DashboardService;
import com.protechone.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var summary = dashboardService.summary();
        var monthly = dashboardService.salesByMonth();
        var topProducts = dashboardService.topSellingProducts();
        var recent = dashboardService.recentTransactions();
        var lowStock = inventoryService.lowStockProducts();

        model.addAttribute("summary", summary);
        model.addAttribute("monthlyRows", toChartRows(monthly.stream().map(p -> new Object[]{p.label(), p.value()}).toList()));
        model.addAttribute("topProductRows", toChartRows(topProducts.stream().map(p -> new Object[]{p.label(), p.value()}).toList()));
        model.addAttribute("recentTransactions", recent);
        model.addAttribute("lowStock", lowStock.size() > 6 ? lowStock.subList(0, 6) : lowStock);
        return "dashboard/dashboard";
    }

    /**
     * Pre-computes a safe (double-based, never throws on non-terminating
     * decimals) percentage-of-max for each point so the Thymeleaf template
     * can render a plain CSS bar width without doing BigDecimal division
     * itself (BigDecimal#divide throws ArithmeticException on repeating
     * decimals, which real sales figures can easily produce).
     */
    private List<ChartRow> toChartRows(List<Object[]> points) {
        double max = points.stream().mapToDouble(p -> ((BigDecimal) p[1]).doubleValue()).max().orElse(1);
        if (max <= 0) max = 1;
        final double safeMax = max;
        return points.stream()
                .map(p -> new ChartRow((String) p[0], (BigDecimal) p[1], (((BigDecimal) p[1]).doubleValue() / safeMax) * 100))
                .toList();
    }

    public record ChartRow(String label, BigDecimal value, double percent) {}
}
