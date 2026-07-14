package com.protechone.service;

import com.protechone.dto.inventory.*;
import com.protechone.entity.*;
import com.protechone.exception.BadRequestException;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.*;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Covers products, categories, warehouses and stock movements (in/out,
 * adjustments, transfers between warehouses). Stock levels are maintained
 * in the stock_levels table and mirrored to an append-only stock_movements
 * ledger for full traceability, satisfying the "Inventory movement history"
 * requirement.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BranchRepository branchRepository;
    private final CurrentUser currentUser;

    // ---------------- Categories ----------------

    public List<ProductCategoryResponse> listCategories() {
        return categoryRepository.findByCompanyId(currentUser.companyId()).stream()
                .map(c -> new ProductCategoryResponse(c.getId(), c.getName(),
                        c.getParent() != null ? c.getParent().getId() : null,
                        c.getParent() != null ? c.getParent().getName() : null))
                .toList();
    }

    @Transactional
    public ProductCategoryResponse createCategory(ProductCategoryRequest request) {
        ProductCategory parent = request.parentId() != null ? categoryRepository.findById(request.parentId()).orElse(null) : null;
        ProductCategory saved = categoryRepository.save(ProductCategory.builder()
                .company(currentUser.get().getCompany())
                .name(request.name())
                .parent(parent)
                .build());
        return new ProductCategoryResponse(saved.getId(), saved.getName(),
                parent != null ? parent.getId() : null, parent != null ? parent.getName() : null);
    }

    // ---------------- Warehouses ----------------

    public List<WarehouseResponse> listWarehouses() {
        return warehouseRepository.findByCompanyId(currentUser.companyId()).stream()
                .map(w -> new WarehouseResponse(w.getId(), w.getName(), w.getCode(), w.getLocation(), w.getIsActive()))
                .toList();
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Branch branch = request.branchId() != null ? branchRepository.findById(request.branchId()).orElse(null) : null;
        Warehouse saved = warehouseRepository.save(Warehouse.builder()
                .company(currentUser.get().getCompany())
                .branch(branch)
                .name(request.name())
                .code(request.code())
                .location(request.location())
                .isActive(true)
                .build());
        return new WarehouseResponse(saved.getId(), saved.getName(), saved.getCode(), saved.getLocation(), saved.getIsActive());
    }

    // ---------------- Products ----------------

    public Page<ProductResponse> listProducts(String search, Pageable pageable) {
        Long companyId = currentUser.companyId();
        Page<Product> page = (search == null || search.isBlank())
                ? productRepository.findByCompanyId(companyId, pageable)
                : productRepository.search(companyId, search, pageable);
        return page.map(this::toResponse);
    }

    public ProductResponse getProduct(Long id) {
        return toResponse(findOwnedProduct(id));
    }

    public List<ProductResponse> lowStockProducts() {
        return stockLevelRepository.findLowStock(currentUser.companyId()).stream()
                .map(sl -> toResponse(sl.getProduct()))
                .distinct()
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Long companyId = currentUser.companyId();
        if (productRepository.existsByCompanyIdAndSkuIgnoreCase(companyId, request.sku())) {
            throw new BadRequestException("A product with SKU '" + request.sku() + "' already exists");
        }
        ProductCategory category = request.categoryId() != null ? categoryRepository.findById(request.categoryId()).orElse(null) : null;

        Product product = productRepository.save(Product.builder()
                .company(currentUser.get().getCompany())
                .category(category)
                .sku(request.sku())
                .barcode(request.barcode())
                .name(request.name())
                .description(request.description())
                .brand(request.brand())
                .unit(request.unit() == null ? "pcs" : request.unit())
                .costPrice(request.costPrice())
                .sellingPrice(request.sellingPrice())
                .taxRate(request.taxRate() == null ? BigDecimal.ZERO : request.taxRate())
                .reorderLevel(request.reorderLevel() == null ? BigDecimal.ZERO : request.reorderLevel())
                .minStock(request.minStock() == null ? BigDecimal.ZERO : request.minStock())
                .maxStock(request.maxStock())
                .trackSerial(Boolean.TRUE.equals(request.trackSerial()))
                .trackBatch(Boolean.TRUE.equals(request.trackBatch()))
                .imageUrl(request.imageUrl())
                .isActive(request.isActive() == null || request.isActive())
                .build());

        if (request.warehouseId() != null && request.openingQuantity() != null
                && request.openingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            applyMovement(product, warehouse, request.openingQuantity(), "IN", "OPENING_STOCK", null, "Opening stock");
        }

        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findOwnedProduct(id);
        ProductCategory category = request.categoryId() != null ? categoryRepository.findById(request.categoryId()).orElse(null) : null;

        product.setSku(request.sku());
        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setUnit(request.unit());
        product.setCategory(category);
        product.setCostPrice(request.costPrice());
        product.setSellingPrice(request.sellingPrice());
        if (request.taxRate() != null) product.setTaxRate(request.taxRate());
        if (request.reorderLevel() != null) product.setReorderLevel(request.reorderLevel());
        if (request.minStock() != null) product.setMinStock(request.minStock());
        product.setMaxStock(request.maxStock());
        if (request.trackSerial() != null) product.setTrackSerial(request.trackSerial());
        if (request.trackBatch() != null) product.setTrackBatch(request.trackBatch());
        product.setImageUrl(request.imageUrl());
        if (request.isActive() != null) product.setIsActive(request.isActive());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findOwnedProduct(id));
    }

    // ---------------- Stock operations ----------------

    @Transactional
    public void adjustStock(StockAdjustmentRequest request) {
        Product product = findOwnedProduct(request.productId());
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        String type = request.movementType().toUpperCase();
        BigDecimal signedQty = type.equals("OUT") ? request.quantity().negate() : request.quantity();
        applyMovement(product, warehouse, signedQty, type, "ADJUSTMENT", null, request.notes());
    }

    @Transactional
    public void transferStock(StockTransferRequest request) {
        Product product = findOwnedProduct(request.productId());
        Warehouse from = warehouseRepository.findById(request.fromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found"));
        Warehouse to = warehouseRepository.findById(request.toWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found"));

        StockLevel fromLevel = stockLevelRepository.findByProductIdAndWarehouseId(product.getId(), from.getId())
                .orElseThrow(() -> new BadRequestException("No stock available in source warehouse"));
        if (fromLevel.getQuantity().compareTo(request.quantity()) < 0) {
            throw new BadRequestException("Insufficient stock in source warehouse for transfer");
        }

        applyMovement(product, from, request.quantity().negate(), "TRANSFER", "TRANSFER", null,
                "Transfer to " + to.getName() + (request.notes() != null ? " - " + request.notes() : ""));
        applyMovement(product, to, request.quantity(), "TRANSFER", "TRANSFER", null,
                "Transfer from " + from.getName() + (request.notes() != null ? " - " + request.notes() : ""));
    }

    /** Adjusts stock_levels and appends a stock_movements ledger entry. Package-private for reuse by Sales/Purchase services. */
    @Transactional
    public void applyMovement(Product product, Warehouse warehouse, BigDecimal signedQuantity, String movementType,
                               String referenceType, Long referenceId, String notes) {
        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> StockLevel.builder()
                        .product(product).warehouse(warehouse)
                        .quantity(BigDecimal.ZERO).reservedQty(BigDecimal.ZERO).build());

        BigDecimal newQty = level.getQuantity().add(signedQuantity);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Stock movement would result in negative stock for " + product.getName());
        }
        level.setQuantity(newQty);
        stockLevelRepository.save(level);

        stockMovementRepository.save(StockMovement.builder()
                .company(product.getCompany())
                .product(product)
                .warehouse(warehouse)
                .movementType(movementType)
                .quantity(signedQuantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .notes(notes)
                .createdBy(currentUser.get())
                .build());
    }

    private Product findOwnedProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (!product.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        return product;
    }

    private ProductResponse toResponse(Product p) {
        BigDecimal totalStock = stockLevelRepository.totalQuantityForProduct(p.getId());
        String status;
        if (totalStock.compareTo(BigDecimal.ZERO) <= 0) status = "Out of Stock";
        else if (p.getReorderLevel() != null && totalStock.compareTo(p.getReorderLevel()) <= 0) status = "Low Stock";
        else status = "Available";

        return new ProductResponse(p.getId(), p.getSku(), p.getBarcode(), p.getName(), p.getDescription(),
                p.getBrand(), p.getUnit(), p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getCostPrice(), p.getSellingPrice(), p.getTaxRate(), p.getReorderLevel(), p.getMinStock(),
                p.getMaxStock(), totalStock, status, p.getIsActive());
    }
}
