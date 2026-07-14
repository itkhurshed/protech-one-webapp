-- ============================================================================
-- ProTech One - Seed Data
-- Creates default roles/permissions, a demo company/branch, an admin login,
-- and a few sample records so the system is usable immediately after setup.
-- Default admin login: admin@protechone.com / Admin@123  (CHANGE AFTER FIRST LOGIN)
-- ============================================================================

-- Roles
INSERT INTO roles (code, name, description, is_system) VALUES
 ('SUPER_ADMIN',        'Super Administrator', 'Full access across all companies and modules', TRUE),
 ('COMPANY_ADMIN',      'Company Administrator', 'Full access within the company', TRUE),
 ('BRANCH_MANAGER',     'Branch Manager', 'Manage a single branch', FALSE),
 ('ACCOUNTANT',         'Accountant', 'Accounting and financial modules', FALSE),
 ('HR_MANAGER',         'HR Manager', 'HR and payroll modules', FALSE),
 ('SALES_MANAGER',      'Sales Manager', 'CRM and sales modules', FALSE),
 ('WAREHOUSE_MANAGER',  'Warehouse Manager', 'Inventory and purchasing modules', FALSE),
 ('CASHIER',            'Cashier', 'Point of sale operations', FALSE),
 ('TECHNICIAN',         'Technician', 'IT help desk and assets', FALSE),
 ('EMPLOYEE',           'Employee', 'Basic employee access', FALSE),
 ('CUSTOMER',           'Customer', 'Customer portal access', FALSE),
 ('SUPPLIER',           'Supplier', 'Supplier portal access', FALSE);

-- Permissions (module-scoped, coarse-grained for Phase 1)
INSERT INTO permissions (code, module, description) VALUES
 ('DASHBOARD_VIEW',   'DASHBOARD', 'View executive dashboard'),
 ('CUSTOMER_MANAGE',  'CRM',       'Create/edit/delete customers'),
 ('SUPPLIER_MANAGE',  'PURCHASING','Create/edit/delete suppliers'),
 ('PRODUCT_MANAGE',   'INVENTORY', 'Create/edit/delete products and stock'),
 ('SALES_MANAGE',     'SALES',     'Create/edit sales invoices'),
 ('PURCHASE_MANAGE',  'PURCHASING','Create/edit purchase invoices'),
 ('EXPENSE_MANAGE',   'FINANCE',   'Create/edit expenses'),
 ('REPORT_VIEW',      'REPORTS',   'View and export reports'),
 ('USER_MANAGE',      'ADMIN',     'Manage users, roles and permissions'),
 ('COMPANY_MANAGE',   'ADMIN',     'Manage company/branch settings');

-- Grant all permissions to SUPER_ADMIN and COMPANY_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('SUPER_ADMIN', 'COMPANY_ADMIN');

-- Sales Manager -> CRM + Sales + Dashboard + Reports
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'SALES_MANAGER'
  AND p.code IN ('DASHBOARD_VIEW','CUSTOMER_MANAGE','SALES_MANAGE','REPORT_VIEW');

-- Warehouse Manager -> Inventory + Purchasing + Dashboard + Reports
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'WAREHOUSE_MANAGER'
  AND p.code IN ('DASHBOARD_VIEW','PRODUCT_MANAGE','SUPPLIER_MANAGE','PURCHASE_MANAGE','REPORT_VIEW');

-- Accountant -> Finance + Reports + Dashboard
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ACCOUNTANT'
  AND p.code IN ('DASHBOARD_VIEW','EXPENSE_MANAGE','REPORT_VIEW');

-- Demo company & branch
INSERT INTO companies (id, name, legal_name, tax_number, email, phone, address, city, country, currency_code, language_code)
VALUES (1, 'ProTech Demo LLC', 'ProTech Demo Limited Liability Company', 'TAX-000123',
        'info@protechone.com', '+1-555-0100', '100 Business Ave', 'Metropolis', 'USA', 'USD', 'en');

INSERT INTO branches (id, company_id, name, code, address, is_main)
VALUES (1, 1, 'Head Office', 'HQ', '100 Business Ave, Metropolis', TRUE);

-- Default admin user (password: Admin@123)
INSERT INTO users (id, company_id, branch_id, role_id, employee_number, first_name, last_name, email, phone,
                    password_hash, is_active, email_verified)
VALUES (1, 1, 1, (SELECT id FROM roles WHERE code = 'COMPANY_ADMIN'), 'EMP-0001', 'System', 'Administrator',
        'admin@protechone.com', '+1-555-0101',
        '$2b$12$dmWvgeA2wwTJ9JAj1Z4.6OyhaPldmVGlFd3gRtkjA9WIEU2u7rea6',
        TRUE, TRUE);

-- Warehouse
INSERT INTO warehouses (id, company_id, branch_id, name, code, location)
VALUES (1, 1, 1, 'Main Warehouse', 'WH-01', 'Head Office Storage');

-- Product categories
INSERT INTO product_categories (id, company_id, name) VALUES
 (1, 1, 'Electronics'),
 (2, 1, 'Office Supplies'),
 (3, 1, 'Furniture');

-- Sample products
INSERT INTO products (id, company_id, category_id, sku, barcode, name, unit, cost_price, selling_price, tax_rate, reorder_level, min_stock)
VALUES
 (1, 1, 1, 'SKU-1001', '8901234500011', 'Wireless Mouse', 'pcs', 8.50, 15.99, 5, 20, 10),
 (2, 1, 1, 'SKU-1002', '8901234500028', '24" Monitor', 'pcs', 95.00, 149.99, 5, 5, 3),
 (3, 1, 2, 'SKU-1003', '8901234500035', 'A4 Paper Ream', 'box', 3.20, 5.99, 5, 50, 20),
 (4, 1, 3, 'SKU-1004', '8901234500042', 'Office Chair', 'pcs', 60.00, 109.00, 5, 8, 4);

INSERT INTO stock_levels (product_id, warehouse_id, quantity) VALUES
 (1, 1, 45), (2, 1, 4), (3, 1, 120), (4, 1, 3);

-- Sample customers & suppliers
INSERT INTO customers (id, company_id, code, name, email, phone, city, country, credit_limit)
VALUES
 (1, 1, 'CUST-001', 'Acme Retail Group', 'purchasing@acmeretail.com', '+1-555-0201', 'Metropolis', 'USA', 10000),
 (2, 1, 'CUST-002', 'Blue Ridge Traders', 'orders@blueridge.com', '+1-555-0202', 'Springfield', 'USA', 5000);

INSERT INTO suppliers (id, company_id, code, name, email, phone, city, country, payment_terms)
VALUES
 (1, 1, 'SUPP-001', 'Global Tech Distributors', 'sales@globaltechdist.com', '+1-555-0301', 'Newark', 'USA', 'Net 30'),
 (2, 1, 'SUPP-002', 'Office World Wholesale', 'info@officeworld.com', '+1-555-0302', 'Trenton', 'USA', 'Net 15');

-- Expense categories
INSERT INTO expense_categories (id, company_id, name) VALUES
 (1, 1, 'Rent'), (2, 1, 'Utilities'), (3, 1, 'Marketing'), (4, 1, 'Office Supplies'), (5, 1, 'Travel');

-- Sequence bump so app-generated IDs don't collide with seed rows
SELECT setval('companies_id_seq', 10);
SELECT setval('branches_id_seq', 10);
SELECT setval('warehouses_id_seq', 10);
SELECT setval('product_categories_id_seq', 10);
SELECT setval('products_id_seq', 100);
SELECT setval('customers_id_seq', 100);
SELECT setval('suppliers_id_seq', 100);
SELECT setval('expense_categories_id_seq', 10);
SELECT setval('users_id_seq', 10);
