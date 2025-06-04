-- Índices para otimizar consultas e resolver N+1 queries

-- Índices para tabela users
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_active_created ON users(active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_name_search ON users USING gin(to_tsvector('portuguese', name));
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_active ON users(email, active);

-- Índices para tabela ongs
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_email ON ongs(email) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_cnpj ON ongs(cnpj) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_active_created ON ongs(active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_endereco ON ongs USING gin(to_tsvector('portuguese', endereco)) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_nome_search ON ongs USING gin(to_tsvector('portuguese', nome_ong)) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_email_active ON ongs(email, active);

-- Índices para tabela protetores
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_email ON protetores(email) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_cpf ON protetores(cpf) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_active_created ON protetores(active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_endereco ON protetores USING gin(to_tsvector('portuguese', endereco)) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_nome_search ON protetores USING gin(to_tsvector('portuguese', nome_completo)) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_capacidade ON protetores(capacidade_acolhimento) WHERE active = true AND capacidade_acolhimento > 0;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_email_active ON protetores(email, active);

-- Índices compostos para consultas frequentes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_type_active ON users(user_type, active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_type_active ON ongs(user_type, active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_type_active ON protetores(user_type, active);

-- Índices para ordenação por timestamp
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_updated_at ON users(updated_at DESC) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ongs_updated_at ON ongs(updated_at DESC) WHERE active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_protetores_updated_at ON protetores(updated_at DESC) WHERE active = true;

-- Estatísticas para o otimizador
ANALYZE users;
ANALYZE ongs;
ANALYZE protetores;