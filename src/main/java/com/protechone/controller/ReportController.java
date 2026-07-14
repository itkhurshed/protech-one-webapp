package com.protechone.controller;

import com.protechone.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                           Model model) {
        model.addAttribute("sales", reportService.salesReport(start, end));
        model.addAttribute("purchases", reportService.purchaseReport(start, end));
        model.addAttribute("expenses", reportService.expenseReport(start, end));
        model.addAttribute("inventory", reportService.inventoryReport());
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "reports/reports";
    }

    /** Server-rendered CSV export — a plain link download, no client-side JS/Blob needed. */
    @GetMapping("/reports/export.csv")
    public void exportCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                           HttpServletResponse response) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.putAll(reportService.salesReport(start, end));
        row.putAll(reportService.purchaseReport(start, end));
        row.putAll(reportService.expenseReport(start, end));
        row.putAll(reportService.inventoryReport());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"protech-one-report.csv\"");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Metric,Value");
            for (Map.Entry<String, Object> e : row.entrySet()) {
                writer.println(e.getKey() + "," + e.getValue());
            }
        }
    }
}
