-- ============================================================
--  LabelForge – PostgreSQL schema
--  Run once against your DB before starting the app.
--  spring.jpa.hibernate.ddl-auto=validate (app does NOT auto-create)
-- ============================================================

-- CREATE DATABASE labelforge;
-- \c labelforge;

CREATE USER labelforge_user WITH PASSWORD 'change_me_in_prod';
GRANT ALL PRIVILEGES ON DATABASE labelforge TO labelforge_user;

-- ─── Sequence for item-reference digit (5-digit, 00001-99999) ──
-- Shared across all products to guarantee globally unique EAN-13s
CREATE SEQUENCE IF NOT EXISTS product_item_ref_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    NO CYCLE;

GRANT USAGE, SELECT ON SEQUENCE product_item_ref_seq TO labelforge_user;

-- ─── Products ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    weight      VARCHAR(100)   NOT NULL,          -- e.g. "500g", "1L"
    price       NUMERIC(10, 2) NOT NULL,
    ean         VARCHAR(13)    NOT NULL UNIQUE,    -- full 13-digit EAN
    item_ref    INTEGER        NOT NULL UNIQUE,    -- 5-digit item reference (from sequence)
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

GRANT ALL PRIVILEGES ON TABLE products TO labelforge_user;
GRANT USAGE, SELECT ON SEQUENCE products_id_seq TO labelforge_user;

-- ─── Auto-update updated_at ────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ─── Indexes ───────────────────────────────────────────────────
CREATE INDEX idx_products_ean        ON products(ean);
CREATE INDEX idx_products_name_lower ON products(LOWER(name));
CREATE INDEX idx_products_created_at ON products(created_at DESC);
