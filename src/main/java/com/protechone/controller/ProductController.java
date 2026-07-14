package com.protechone.controller;

import com.protechone.dto.inventory.ProductRequest;
import com.protechone.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var result = inventoryService.listProducts(search, PageRequest.of(page, 20, Sort.by("name").ascending()));
        model.addAttribute("products", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("search", search);
        return "inventory/products-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String newForm(Model model) {
        model.addAttribute("form", new ProductForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", inventoryService.listCategories());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        return "inventory/product-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String create(@ModelAttribute ProductForm form, RedirectAttributes redirectAttributes) {
        inventoryService.createProduct(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        var p = inventoryService.getProduct(id);
        ProductForm form = new ProductForm();
        form.setSku(p.sku()); form.setBarcode(p.barcode()); form.setName(p.name()); form.setDescription(p.description());
        form.setBrand(p.brand()); form.setUnit(p.unit()); form.setCategoryId(p.categoryId());
        form.setCostPrice(p.costPrice()); form.setSellingPrice(p.sellingPrice()); form.setTaxRate(p.taxRate());
        form.setReorderLevel(p.reorderLevel()); form.setMinStock(p.minStock()); form.setMaxStock(p.maxStock());
        form.setIsActive(p.isActive());
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("productId", id);
        model.addAttribute("categories", inventoryService.listCategories());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        return "inventory/product-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String update(@PathVariable Long id, @ModelAttribute ProductForm form, RedirectAttributes redirectAttributes) {
        inventoryService.updateProduct(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
        return "redirect:/products";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        model.addAttribute("product", inventoryService.getProduct(id));
        return "inventory/product-delete-confirm";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        inventoryService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted.");
        return "redirect:/products";
    }

    @PostMapping("/category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String createCategory(@RequestParam String name, RedirectAttributes redirectAttributes) {
        inventoryService.createCategory(new com.protechone.dto.inventory.ProductCategoryRequest(name, null));
        redirectAttributes.addFlashAttribute("successMessage", "Category created.");
        return "redirect:/products/new";
    }

    @Data
    public static class ProductForm {
        private String sku, barcode, name, description, brand, unit = "pcs", imageUrl;
        private Long categoryId, warehouseId;
        private BigDecimal costPrice = BigDecimal.ZERO;
        private BigDecimal sellingPrice = BigDecimal.ZERO;
        private BigDecimal taxRate = BigDecimal.ZERO;
        private BigDecimal reorderLevel = BigDecimal.ZERO;
        private BigDecimal minStock = BigDecimal.ZERO;
        private BigDecimal maxStock;
        private BigDecimal openingQuantity = BigDecimal.ZERO;
        private Boolean trackSerial = false;
        private Boolean trackBatch = false;
        private Boolean isActive = true;

        ProductRequest toRequest() {
            return new ProductRequest(sku, barcode, name, description, brand, unit, categoryId, costPrice,
                    sellingPrice, taxRate, reorderLevel, minStock, maxStock, trackSerial, trackBatch, imageUrl,
                    isActive, warehouseId, openingQuantity);
        }
    }
}
