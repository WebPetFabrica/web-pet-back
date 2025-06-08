-- WebPet Database Migration V3: Create pets and donations tables
-- Adds support for pet management and donation tracking

-- Create pets table
CREATE TABLE pets (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    nome VARCHAR(100) NOT NULL,
    especie VARCHAR(20) NOT NULL,
    raca VARCHAR(50) NOT NULL,
    genero VARCHAR(10) NOT NULL,
    porte VARCHAR(15) NOT NULL,
    data_nascimento DATE NOT NULL,
    descricao TEXT,
    foto_url VARCHAR(500),
    status_adocao VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    ativo BOOLEAN NOT NULL DEFAULT true,
    responsavel_id VARCHAR(36) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pets_responsavel FOREIGN KEY (responsavel_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_pets_especie CHECK (
        especie IN ('CACHORRO', 'GATO', 'PASSARO', 'COELHO', 'HAMSTER', 'PEIXE', 'TARTARUGA', 'OUTRO')
    ),
    
    CONSTRAINT chk_pets_genero CHECK (
        genero IN ('MACHO', 'FEMEA')
    ),
    
    CONSTRAINT chk_pets_porte CHECK (
        porte IN ('PEQUENO', 'MEDIO', 'GRANDE', 'GIGANTE')
    ),
    
    CONSTRAINT chk_pets_status_adocao CHECK (
        status_adocao IN ('DISPONIVEL', 'EM_PROCESSO', 'ADOTADO', 'INDISPONIVEL')
    ),
    
    CONSTRAINT chk_pets_data_nascimento_valid CHECK (
        data_nascimento <= CURRENT_DATE
    )
);

-- Add foreign key constraints for ONG and Protetor tables
ALTER TABLE pets ADD CONSTRAINT fk_pets_responsavel_ongs 
    FOREIGN KEY (responsavel_id) REFERENCES ongs(id) ON DELETE CASCADE;

ALTER TABLE pets ADD CONSTRAINT fk_pets_responsavel_protetores 
    FOREIGN KEY (responsavel_id) REFERENCES protetores(id) ON DELETE CASCADE;

-- Create doacoes table
CREATE TABLE doacoes (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    valor DECIMAL(10,2) NOT NULL,
    tipo_doacao VARCHAR(20) NOT NULL,
    mensagem TEXT,
    nome_doador VARCHAR(100) NOT NULL,
    email_doador VARCHAR(255),
    telefone_doador VARCHAR(20),
    status_doacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    transaction_id VARCHAR(100),
    beneficiario_id VARCHAR(36) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processado_em TIMESTAMP,
    
    CONSTRAINT fk_doacoes_beneficiario_users FOREIGN KEY (beneficiario_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_doacoes_beneficiario_ongs FOREIGN KEY (beneficiario_id) 
        REFERENCES ongs(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_doacoes_beneficiario_protetores FOREIGN KEY (beneficiario_id) 
        REFERENCES protetores(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_doacoes_valor_positive CHECK (
        valor > 0
    ),
    
    CONSTRAINT chk_doacoes_tipo CHECK (
        tipo_doacao IN ('MONETARIA', 'RACAO', 'MEDICAMENTOS', 'BRINQUEDOS', 'ACESSORIOS', 'OUTRO')
    ),
    
    CONSTRAINT chk_doacoes_status CHECK (
        status_doacao IN ('PENDENTE', 'PROCESSADA', 'FALHA', 'CANCELADA')
    ),
    
    CONSTRAINT chk_doacoes_email_format CHECK (
        email_doador IS NULL OR 
        email_doador ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    )
);

-- Create indexes for performance optimization
-- Pets indexes
CREATE INDEX idx_pets_especie ON pets(especie) WHERE ativo = true;
CREATE INDEX idx_pets_genero ON pets(genero) WHERE ativo = true;
CREATE INDEX idx_pets_porte ON pets(porte) WHERE ativo = true;
CREATE INDEX idx_pets_status_adocao ON pets(status_adocao) WHERE ativo = true;
CREATE INDEX idx_pets_responsavel ON pets(responsavel_id) WHERE ativo = true;
CREATE INDEX idx_pets_ativo_criado ON pets(ativo, criado_em DESC);
CREATE INDEX idx_pets_data_nascimento ON pets(data_nascimento) WHERE ativo = true;
CREATE INDEX idx_pets_raca ON pets(raca) WHERE ativo = true;

-- Composite index for common filtering queries
CREATE INDEX idx_pets_filters ON pets(ativo, status_adocao, especie, genero, porte, criado_em DESC) 
    WHERE ativo = true AND status_adocao = 'DISPONIVEL';

-- Text search index for pets
CREATE INDEX idx_pets_text_search ON pets USING gin(
    to_tsvector('portuguese', 
        COALESCE(nome, '') || ' ' || 
        COALESCE(raca, '') || ' ' || 
        COALESCE(descricao, '')
    )
) WHERE ativo = true;

-- Donations indexes
CREATE INDEX idx_doacoes_beneficiario ON doacoes(beneficiario_id);
CREATE INDEX idx_doacoes_status ON doacoes(status_doacao);
CREATE INDEX idx_doacoes_tipo ON doacoes(tipo_doacao);
CREATE INDEX idx_doacoes_criado_em ON doacoes(criado_em DESC);
CREATE INDEX idx_doacoes_beneficiario_status ON doacoes(beneficiario_id, status_doacao);
CREATE INDEX idx_doacoes_processado_em ON doacoes(processado_em DESC) WHERE processado_em IS NOT NULL;

-- Text search index for donations
CREATE INDEX idx_doacoes_text_search ON doacoes USING gin(
    to_tsvector('portuguese', 
        COALESCE(nome_doador, '') || ' ' || 
        COALESCE(email_doador, '') || ' ' || 
        COALESCE(mensagem, '')
    )
);

-- Apply updated_at triggers to new tables
CREATE TRIGGER update_pets_updated_at 
    BEFORE UPDATE ON pets 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create partial unique indexes for business rules
CREATE UNIQUE INDEX idx_pets_unique_active_name_responsavel 
    ON pets(nome, responsavel_id) 
    WHERE ativo = true;

-- Create statistics for query optimization
CREATE STATISTICS pets_multi_column_stats ON especie, genero, porte, status_adocao FROM pets;
CREATE STATISTICS doacoes_multi_column_stats ON beneficiario_id, status_doacao, tipo_doacao FROM doacoes;

-- Add comments for documentation
COMMENT ON TABLE pets IS 'Table storing pet information for adoption';
COMMENT ON TABLE doacoes IS 'Table storing donation information';

COMMENT ON COLUMN pets.especie IS 'Species of the pet (CACHORRO, GATO, etc.)';
COMMENT ON COLUMN pets.genero IS 'Gender of the pet (MACHO, FEMEA)';
COMMENT ON COLUMN pets.porte IS 'Size of the pet (PEQUENO, MEDIO, GRANDE, GIGANTE)';
COMMENT ON COLUMN pets.status_adocao IS 'Adoption status (DISPONIVEL, EM_PROCESSO, ADOTADO, INDISPONIVEL)';
COMMENT ON COLUMN pets.ativo IS 'Soft delete flag for pets';
COMMENT ON COLUMN pets.responsavel_id IS 'References the responsible user (ONG, Protetor, or User)';

COMMENT ON COLUMN doacoes.tipo_doacao IS 'Type of donation (MONETARIA, RACAO, etc.)';
COMMENT ON COLUMN doacoes.status_doacao IS 'Processing status (PENDENTE, PROCESSADA, FALHA, CANCELADA)';
COMMENT ON COLUMN doacoes.beneficiario_id IS 'References the beneficiary user (ONG, Protetor, or User)';
COMMENT ON COLUMN doacoes.transaction_id IS 'External payment processor transaction ID';

-- Create sample data for testing (optional - remove in production)
-- INSERT INTO pets (nome, especie, raca, genero, porte, data_nascimento, descricao, responsavel_id)
-- SELECT 'Rex', 'CACHORRO', 'Labrador', 'MACHO', 'GRANDE', '2020-01-15', 'Cão muito carinhoso e brincalhão', id
-- FROM ongs LIMIT 1;