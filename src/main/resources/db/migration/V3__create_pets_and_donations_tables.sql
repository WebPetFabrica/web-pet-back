-- =============================================================================
-- Migration V3: Pet Adoption and Donations System
-- Description: Creates pets and donations tables for core business functionality
-- Author: WebPet Team
-- Date: 2025-06-09
-- =============================================================================

-- =============================================================================
-- CREATE PETS TABLE
-- =============================================================================

CREATE TABLE pets (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    nome VARCHAR(255) NOT NULL,
    especie VARCHAR(20) NOT NULL CHECK (especie IN ('CACHORRO', 'GATO')),
    porte VARCHAR(20) NOT NULL CHECK (porte IN ('PEQUENO', 'MEDIO', 'GRANDE')),
    genero VARCHAR(20) NOT NULL CHECK (genero IN ('MACHO', 'FEMEA')),
    idade INTEGER NOT NULL CHECK (idade >= 0 AND idade <= 30),
    responsavel_id VARCHAR(36) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT true,
    descricao TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- CREATE DONATIONS TABLE
-- =============================================================================

CREATE TABLE doacoes (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    doador_id VARCHAR(36) NOT NULL,
    beneficiario_id VARCHAR(36) NOT NULL,
    valor DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    data_doacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- =============================================================================

-- Pets table indexes
CREATE INDEX idx_pets_especie ON pets(especie) WHERE disponivel = true;
CREATE INDEX idx_pets_porte ON pets(porte) WHERE disponivel = true;
CREATE INDEX idx_pets_genero ON pets(genero) WHERE disponivel = true;
CREATE INDEX idx_pets_idade ON pets(idade) WHERE disponivel = true;
CREATE INDEX idx_pets_disponivel ON pets(disponivel);
CREATE INDEX idx_pets_responsavel_id ON pets(responsavel_id);
CREATE INDEX idx_pets_created_at ON pets(created_at DESC);
CREATE INDEX idx_pets_responsavel_disponivel ON pets(responsavel_id, disponivel);

-- Composite indexes for common filtering scenarios
CREATE INDEX idx_pets_especie_porte_disponivel ON pets(especie, porte, disponivel) WHERE disponivel = true;
CREATE INDEX idx_pets_especie_idade_disponivel ON pets(especie, idade, disponivel) WHERE disponivel = true;
CREATE INDEX idx_pets_search_filters ON pets(especie, porte, genero, idade) WHERE disponivel = true;

-- Donations table indexes
CREATE INDEX idx_doacoes_doador_id ON doacoes(doador_id);
CREATE INDEX idx_doacoes_beneficiario_id ON doacoes(beneficiario_id);
CREATE INDEX idx_doacoes_data_doacao ON doacoes(data_doacao DESC);
CREATE INDEX idx_doacoes_valor ON doacoes(valor);
CREATE INDEX idx_doacoes_doador_data ON doacoes(doador_id, data_doacao DESC);
CREATE INDEX idx_doacoes_beneficiario_data ON doacoes(beneficiario_id, data_doacao DESC);

-- =============================================================================
-- CONSTRAINTS AND VALIDATIONS
-- =============================================================================

-- Pet constraints
ALTER TABLE pets 
    ADD CONSTRAINT chk_pets_nome_not_empty 
    CHECK (LENGTH(TRIM(nome)) > 0);

ALTER TABLE pets 
    ADD CONSTRAINT chk_pets_nome_length 
    CHECK (LENGTH(nome) <= 255);

ALTER TABLE pets 
    ADD CONSTRAINT chk_pets_descricao_length 
    CHECK (descricao IS NULL OR LENGTH(descricao) <= 2000);

-- Donation constraints
ALTER TABLE doacoes 
    ADD CONSTRAINT chk_doacoes_different_users 
    CHECK (doador_id != beneficiario_id);

ALTER TABLE doacoes 
    ADD CONSTRAINT chk_doacoes_data_not_future 
    CHECK (data_doacao <= CURRENT_TIMESTAMP);

-- =============================================================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- =============================================================================

-- Apply updated_at triggers using existing function
CREATE TRIGGER trigger_pets_updated_at
    BEFORE UPDATE ON pets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_doacoes_updated_at
    BEFORE UPDATE ON doacoes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- TABLE COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE pets IS 'Stores information about pets available for adoption';
COMMENT ON TABLE doacoes IS 'Stores donation transactions between users';

-- Pet column comments
COMMENT ON COLUMN pets.id IS 'Unique identifier for the pet';
COMMENT ON COLUMN pets.nome IS 'Pet name (required, max 255 characters)';
COMMENT ON COLUMN pets.especie IS 'Pet species: CACHORRO or GATO';
COMMENT ON COLUMN pets.porte IS 'Pet size: PEQUENO, MEDIO, or GRANDE';
COMMENT ON COLUMN pets.genero IS 'Pet gender: MACHO or FEMEA';
COMMENT ON COLUMN pets.idade IS 'Pet age in years (0-30)';
COMMENT ON COLUMN pets.responsavel_id IS 'ID of the responsible user (ONG or PROTETOR)';
COMMENT ON COLUMN pets.disponivel IS 'Whether the pet is available for adoption';
COMMENT ON COLUMN pets.descricao IS 'Optional description of the pet (max 2000 characters)';

-- Donation column comments
COMMENT ON COLUMN doacoes.id IS 'Unique identifier for the donation';
COMMENT ON COLUMN doacoes.doador_id IS 'ID of the user making the donation';
COMMENT ON COLUMN doacoes.beneficiario_id IS 'ID of the user receiving the donation (ONG or PROTETOR)';
COMMENT ON COLUMN doacoes.valor IS 'Donation amount in Brazilian Real (BRL)';
COMMENT ON COLUMN doacoes.data_doacao IS 'Timestamp when the donation was made';