-- Otimiza a consulta de busca de pets com filtros comuns
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pets_search_filter
ON pets (disponivel, especie, porte, genero, idade, created_at DESC);