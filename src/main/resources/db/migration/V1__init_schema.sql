-- ============================================================================
-- ProTech One - Phase 1 Core Schema
-- Modules: Company/Branch, Users/Roles/Permissions, CRM (Customers/Suppliers),
--          Inventory (Products/Warehouses/Stock), Sales, Purchasing, Expenses,
--          Audit/Login history, System settings.
-- ============================================================================

-- ------------------------------------------------------------------
-- Company & Branch
-- ------------------------------------------------------------------
CREATE TABLE companies (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(200) NOT NULL,
    legal_name              VARCHAR(200),
    tax_number              VARCHAR(100),
    commercial_registration VARCHAR(100),
    email                   VARCHAR(150),
    phone                   VARCHAR(50),
    address                 VARCHAR(300),
    city                    VARCHAR(100),
    country                 VARCHAR(100),
    logo_url                VARCHAR(300),
    currency_code           VARCHAR(10) DEFAULT 'USD',
    language_code           VARCHAR(10) DEFAULT 'en',
    fiscal_year_start_month SMALLINT DEFAULT 1,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE branches (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name         VARCHAR(150) NOT NULL,
    code         VARCHAR(30),
    address      VARCHAR(300),
    phone        VARCHAR(50),
    is_main      BOOLEAN NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, code)
);

-- ------------------------------------------------------------------
-- Roles, Permissions, Users
-- ------------------------------------------------------------------
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(300),
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    module      VARCHAR(50) NOT NULL,
    description VARCHAR(300)
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id                   BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    branch_id            BIGINT REFERENCES branches(id) ON DELETE SET NULL,
    role_id              BIGINT NOT NULL REFERENCES roles(id),
    employee_number      VARCHAR(30),
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    email                VARCHAR(150) NOT NULL UNIQUE,
    phone                VARCHAR(50),
    password_hash        VARCHAR(200) NOT NULL,
    avatar_url            VARCHAR(300),
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked            BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts SMALLINT NOT NULL DEFAULT 0,
    mfa_enabled          BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret           VARCHAR(100),
    last_login_at        TIMESTAMP,
    password_reset_token  VARCHAR(200),
    password_reset_expires TIMESTAMP,
    email_verified       BOOLEAN NOT NULL DEFAULT FALSE,
    email_verify_token    VARCHAR(200),
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE login_history (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
    email_tried VARCHAR(150),
    ip_address  VARCHAR(60),
    user_agent  VARCHAR(300),
    success     BOOLEAN NOT NULL,
    reason      VARCHAR(150),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT REFERENCES companies(id) ON DELETE CASCADE,
    user_id      BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(80) NOT NULL,
    entity_id    BIGINT,
    details      TEXT,
    ip_address   VARCHAR(60),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------------
-- CRM: Customers & Suppliers
-- ------------------------------------------------------------------
CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code            VARCHAR(30),
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(50),
    email           VARCHAR(150),
    phone           VARCHAR(50),
    whatsapp        VARCHAR(50),
    address         VARCHAR(300),
    city            VARCHAR(100),
    country         VARCHAR(100),
    tax_number      VARCHAR(100),
    credit_limit    NUMERIC(18,2) DEFAULT 0,
    opening_balance NUMERIC(18,2) DEFAULT 0,
    notes           TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, code)
);

CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code            VARCHAR(30),
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(150),
    phone           VARCHAR(50),
    address         VARCHAR(300),
    city            VARCHAR(100),
    country         VARCHAR(100),
    tax_number      VARCHAR(100),
    opening_balance NUMERIC(18,2) DEFAULT 0,
    payment_terms   VARCHAR(100),
    notes           TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, code)
);

-- ------------------------------------------------------------------
-- Inventory: Categories, Products, Warehouses, Stock
-- ------------------------------------------------------------------
CREATE TABLE product_categories (
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name       VARCHAR(120) NOT NULL,
    parent_id  BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE warehouses (
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    branch_id  BIGINT REFERENCES branches(id) ON DELETE SET NULL,
    name       VARCHAR(150) NOT NULL,
    code       VARCHAR(30),
    location   VARCHAR(300),
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, code)
);

CREATE TABLE products (
    id                BIGSERIAL PRIMARY KEY,
    company_id        BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    category_id       BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    sku               VARCHAR(60) NOT NULL,
    barcode           VARCHAR(60),
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    brand             VARCHAR(100),
    unit              VARCHAR(30) DEFAULT 'pcs',
    cost_price        NUMERIC(18,2) NOT NULL DEFAULT 0,
    selling_price     NUMERIC(18,2) NOT NULL DEFAULT 0,
    tax_rate          NUMERIC(5,2) NOT NULL DEFAULT 0,
    reorder_level     NUMERIC(18,2) NOT NULL DEFAULT 0,
    min_stock         NUMERIC(18,2) NOT NULL DEFAULT 0,
    max_stock         NUMERIC(18,2),
    track_serial      BOOLEAN NOT NULL DEFAULT FALSE,
    track_batch       BOOLEAN NOT NULL DEFAULT FALSE,
    image_url         VARCHAR(300),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, sku)
);

CREATE TABLE stock_levels (
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    quantity     NUMERIC(18,3) NOT NULL DEFAULT 0,
    reserved_qty NUMERIC(18,3) NOT NULL DEFAULT 0,
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, warehouse_id)
);

CREATE TABLE stock_movements (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    product_id     BIGINT NOT NULL REFERENCES products(id),
    warehouse_id   BIGINT NOT NULL REFERENCES warehouses(id),
    movement_type  VARCHAR(20) NOT NULL, -- IN, OUT, TRANSFER, ADJUSTMENT
    quantity       NUMERIC(18,3) NOT NULL,
    reference_type VARCHAR(40),          -- SALES_INVOICE, PURCHASE_INVOICE, ADJUSTMENT, TRANSFER
    reference_id   BIGINT,
    notes          VARCHAR(300),
    created_by     BIGINT REFERENCES users(id),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------------
-- Sales
-- ------------------------------------------------------------------
CREATE TABLE sales_invoices (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    branch_id      BIGINT REFERENCES branches(id),
    warehouse_id   BIGINT REFERENCES warehouses(id),
    customer_id    BIGINT NOT NULL REFERENCES customers(id),
    invoice_number VARCHAR(40) NOT NULL,
    invoice_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date       DATE,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED
    subtotal       NUMERIC(18,2) NOT NULL DEFAULT 0,
    discount_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    tax_total      NUMERIC(18,2) NOT NULL DEFAULT 0,
    grand_total    NUMERIC(18,2) NOT NULL DEFAULT 0,
    amount_paid    NUMERIC(18,2) NOT NULL DEFAULT 0,
    notes          TEXT,
    created_by     BIGINT REFERENCES users(id),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, invoice_number)
);

CREATE TABLE sales_invoice_items (
    id              BIGSERIAL PRIMARY KEY,
    sales_invoice_id BIGINT NOT NULL REFERENCES sales_invoices(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        NUMERIC(18,3) NOT NULL,
    unit_price      NUMERIC(18,2) NOT NULL,
    discount_pct    NUMERIC(5,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5,2) NOT NULL DEFAULT 0,
    line_total      NUMERIC(18,2) NOT NULL
);

-- ------------------------------------------------------------------
-- Purchasing
-- ------------------------------------------------------------------
CREATE TABLE purchase_invoices (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    branch_id      BIGINT REFERENCES branches(id),
    warehouse_id   BIGINT REFERENCES warehouses(id),
    supplier_id    BIGINT NOT NULL REFERENCES suppliers(id),
    invoice_number VARCHAR(40) NOT NULL,
    invoice_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date       DATE,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED
    subtotal       NUMERIC(18,2) NOT NULL DEFAULT 0,
    discount_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    tax_total      NUMERIC(18,2) NOT NULL DEFAULT 0,
    grand_total    NUMERIC(18,2) NOT NULL DEFAULT 0,
    amount_paid    NUMERIC(18,2) NOT NULL DEFAULT 0,
    notes          TEXT,
    created_by     BIGINT REFERENCES users(id),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (company_id, invoice_number)
);

CREATE TABLE purchase_invoice_items (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_invoice_id BIGINT NOT NULL REFERENCES purchase_invoices(id) ON DELETE CASCADE,
    product_id          BIGINT NOT NULL REFERENCES products(id),
    quantity            NUMERIC(18,3) NOT NULL,
    unit_cost           NUMERIC(18,2) NOT NULL,
    discount_pct        NUMERIC(5,2) NOT NULL DEFAULT 0,
    tax_rate            NUMERIC(5,2) NOT NULL DEFAULT 0,
    line_total          NUMERIC(18,2) NOT NULL
);

-- ------------------------------------------------------------------
-- Expenses
-- ------------------------------------------------------------------
CREATE TABLE expense_categories (
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name       VARCHAR(120) NOT NULL,
    UNIQUE (company_id, name)
);

CREATE TABLE expenses (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    branch_id    BIGINT REFERENCES branches(id),
    category_id  BIGINT REFERENCES expense_categories(id),
    expense_date DATE NOT NULL DEFAULT CURRENT_DATE,
    reference_no VARCHAR(40),
    payee        VARCHAR(200),
    amount       NUMERIC(18,2) NOT NULL,
    payment_method VARCHAR(30) DEFAULT 'CASH', -- CASH, BANK, CARD, CHEQUE
    notes        TEXT,
    attachment_url VARCHAR(300),
    created_by   BIGINT REFERENCES users(id),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------------
-- Notifications & Settings
-- ------------------------------------------------------------------
CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id    BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    message    VARCHAR(500),
    type       VARCHAR(30) DEFAULT 'INFO', -- INFO, WARNING, SUCCESS, DANGER
    link       VARCHAR(300),
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE system_settings (
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value VARCHAR(500),
    UNIQUE (company_id, setting_key)
);

-- ------------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------------
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_customers_company ON customers(company_id);
CREATE INDEX idx_customers_name ON customers(name);
CREATE INDEX idx_suppliers_company ON suppliers(company_id);
CREATE INDEX idx_suppliers_name ON suppliers(name);
CREATE INDEX idx_products_company ON products(company_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_sales_invoices_company ON sales_invoices(company_id);
CREATE INDEX idx_sales_invoices_customer ON sales_invoices(customer_id);
CREATE INDEX idx_sales_invoices_date ON sales_invoices(invoice_date);
CREATE INDEX idx_purchase_invoices_company ON purchase_invoices(company_id);
CREATE INDEX idx_purchase_invoices_supplier ON purchase_invoices(supplier_id);
CREATE INDEX idx_expenses_company ON expenses(company_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_audit_logs_company ON audit_logs(company_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
