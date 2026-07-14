package com.protechone.controller;

import com.protechone.dto.inventory.StockAdjustmentRequest;
import com.protechone.dto.inventory.StockTransferRequest;
import com.protechone.dto.inventory.WarehouseRequest;
import com.protechone.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class WarehouseController {

    private final InventoryService inventoryService;

    @GetMapping("/warehouses")
    public String list(Model model) {
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        return "inventory/warehouses-list";
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String create(@RequestParam String name, @RequestParam(required = false) String code,
                          @RequestParam(required = false) String location, RedirectAttributes redirectAttributes) {
        inventoryService.createWarehouse(new WarehouseRequest(name, code, location, null));
        redirectAttributes.addFlashAttribute("successMessage", "Warehouse created.");
        return "redirect:/warehouses";
    }

    @GetMapping("/warehouses/adjust")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String adjustForm(Model model) {
        model.addAttribute("form", new AdjustForm());
        model.addAttribute("products", inventoryService.listProducts(null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        return "inventory/stock-adjust-form";
    }

    @PostMapping("/warehouses/adjust")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String adjust(@ModelAttribute AdjustForm form, RedirectAttributes redirectAttributes) {
        inventoryService.adjustStock(new StockAdjustmentRequest(form.getProductId(), form.getWarehouseId(),
                form.getQuantity(), form.getMovementType(), form.getNotes()));
        redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully.");
        return "redirect:/warehouses";
    }

    @GetMapping("/warehouses/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String transferForm(Model model) {
        model.addAttribute("form", new TransferForm());
        model.addAttribute("products", inventoryService.listProducts(null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        return "inventory/stock-transfer-form";
    }

    @PostMapping("/warehouses/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String transfer(@ModelAttribute TransferForm form, RedirectAttributes redirectAttributes) {
        inventoryService.transferStock(new StockTransferRequest(form.getProductId(), form.getFromWarehouseId(),
                form.getToWarehouseId(), form.getQuantity(), form.getNotes()));
        redirectAttributes.addFlashAttribute("successMessage", "Stock transferred successfully.");
        return "redirect:/warehouses";
    }

    @Data
    public static class AdjustForm {
        private Long productId, warehouseId;
        private BigDecimal quantity;
        private String movementType = "IN";
        private String notes;
    }

    @Data
    public static class TransferForm {
        private Long productId, fromWarehouseId, toWarehouseId;
        private BigDecimal quantity;
        private String notes;
    }
}
