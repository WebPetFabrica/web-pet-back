# Changelog

## [0.0.1] - 2025-05-26

### Added

- Implementação de tipos de usuário: User, ONG e Protetor
- Sistema de herança com `BaseUser` abstrato
- Autenticação JWT unificada para todos os tipos
- Endpoints específicos de registro:
  - `/auth/register` - Usuário comum
  - `/auth/register/ong` - ONGs
  - `/auth/register/protetor` - Protetores
- Validações com Bean Validation
- Tratamento global de exceções
- Documentação completa da API

### Changed

- Refatoração da estrutura de autenticação
- Melhoria no sistema de CORS
- Atualização das dependências do Spring Boot

### Security

- Implementação de BCrypt para senhas
- Tokens JWT com expiração de 2 horas
- Filtros de segurança por tipo de usuário

### Technical Debt

- [ ] Implementar refresh token
- [ ] Adicionar testes de integração
- [ ] Configurar CI/CD
