-- WebPet Initial Database Schema
-- Flyway Migration V1: Create base user tables with inheritance structure

-- Create users table (base for common users)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    user_type VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255)
);

-- Create ongs table (for organizations)
CREATE TABLE ongs (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    user_type VARCHAR(20) NOT NULL DEFAULT 'ONG',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cnpj VARCHAR(18) UNIQUE NOT NULL,
    nome_ong VARCHAR(255) NOT NULL,
    endereco TEXT,
    descricao TEXT
);

-- Create protetores table (for independent protectors)
CREATE TABLE protetores (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    user_type VARCHAR(20) NOT NULL DEFAULT 'PROTETOR',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nome_completo VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    endereco TEXT,
    capacidade_acolhimento INTEGER DEFAULT 0
);

-- Create indexes for performance optimization
CREATE INDEX idx_users_email ON users(email) WHERE active = true;
CREATE INDEX idx_users_active_created ON users(active, created_at DESC);
CREATE INDEX idx_users_user_type ON users(user_type);

CREATE INDEX idx_ongs_email ON ongs(email) WHERE active = true;
CREATE INDEX idx_ongs_cnpj ON ongs(cnpj) WHERE active = true;
CREATE INDEX idx_ongs_active_created ON ongs(active, created_at DESC);
CREATE INDEX idx_ongs_user_type ON ongs(user_type);

CREATE INDEX idx_protetores_email ON protetores(email) WHERE active = true;
CREATE INDEX idx_protetores_cpf ON protetores(cpf) WHERE active = true;
CREATE INDEX idx_protetores_active_created ON protetores(active, created_at DESC);
CREATE INDEX idx_protetores_user_type ON protetores(user_type);
CREATE INDEX idx_protetores_capacidade ON protetores(capacidade_acolhimento) WHERE active = true;

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language plpgsql;

-- Apply updated_at triggers to all tables
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ongs_updated_at 
    BEFORE UPDATE ON ongs 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_protetores_updated_at 
    BEFORE UPDATE ON protetores 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add constraints for data integrity
ALTER TABLE users ADD CONSTRAINT chk_users_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE ongs ADD CONSTRAINT chk_ongs_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE ongs ADD CONSTRAINT chk_ongs_cnpj_format 
    CHECK (cnpj ~ '^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$' OR cnpj ~ '^\d{14}$');

ALTER TABLE protetores ADD CONSTRAINT chk_protetores_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE protetores ADD CONSTRAINT chk_protetores_cpf_format 
    CHECK (cpf ~ '^\d{3}\.\d{3}\.\d{3}-\d{2}$' OR cpf ~ '^\d{11}$');

ALTER TABLE protetores ADD CONSTRAINT chk_protetores_capacidade_positive 
    CHECK (capacidade_acolhimento >= 0);

-- Create comments for documentation
COMMENT ON TABLE users IS 'Base table for regular users who want to adopt pets';
COMMENT ON TABLE ongs IS 'Table for Non-Governmental Organizations';  
COMMENT ON TABLE protetores IS 'Table for independent animal protectors';

COMMENT ON COLUMN users.user_type IS 'Always USER for this table';
COMMENT ON COLUMN ongs.user_type IS 'Always ONG for this table';
COMMENT ON COLUMN protetores.user_type IS 'Always PROTETOR for this table';

COMMENT ON COLUMN users.active IS 'Soft delete flag - false means deactivated account';
COMMENT ON COLUMN ongs.active IS 'Soft delete flag - false means deactivated account';
COMMENT ON COLUMN protetores.active IS 'Soft delete flag - false means deactivated account';