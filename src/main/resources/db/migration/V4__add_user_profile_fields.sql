-- Adiciona colunas de perfil à tabela de usuários
ALTER TABLE users ADD COLUMN bio VARCHAR(255);
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);