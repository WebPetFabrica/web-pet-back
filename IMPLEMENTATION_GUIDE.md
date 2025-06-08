# WebPet Backend - Implementation Guide

## Overview

This guide documents the implementation of the pet adoption platform backend for the Software Factory course project at UTFPR. The implementation includes full CRUD operations for pets and donations, advanced filtering, caching, and comprehensive testing.

## Project Structure

### Domain Entities

#### Pet Entity
- **Location**: `src/main/java/br/edu/utfpr/alunos/webpet/domain/pet/Pet.java`
- **Features**:
  - Complete pet information management
  - Relationship with ONG/Protetor (responsible parties)
  - Adoption status tracking
  - Soft delete capability
  - Age calculation methods

#### Donation Entity  
- **Location**: `src/main/java/br/edu/utfpr/alunos/webpet/domain/donation/Donation.java`
- **Features**:
  - Monetary and item donations
  - Transaction tracking
  - Status management (pending, processed, failed, cancelled)
  - Donor information storage

### API Endpoints

#### Pet Management

```bash
# Create a new pet
POST /pets
{
  "nome": "Rex",
  "especie": "CACHORRO",
  "raca": "Labrador",
  "genero": "MACHO",
  "porte": "GRANDE",
  "dataNascimento": "2020-01-15",
  "descricao": "Cão muito carinhoso",
  "fotoUrl": "https://example.com/photo.jpg"
}

# Get pet by ID
GET /pets/{id}

# List pets with filtering
GET /pets?especie=CACHORRO&genero=MACHO&porte=GRANDE&idadeMinima=2&idadeMaxima=8

# Search pets
GET /pets?search=Rex

# Get user's pets
GET /pets/my-pets

# Update pet
PUT /pets/{id}

# Delete pet (soft delete)
DELETE /pets/{id}

# Update adoption status
PATCH /pets/{id}/status/adotado
PATCH /pets/{id}/status/disponivel
PATCH /pets/{id}/status/indisponivel

# Get metadata
GET /pets/metadata/especies
GET /pets/metadata/portes
GET /pets/metadata/generos
GET /pets/metadata/racas
```

#### Donation Management

```bash
# Create donation
POST /doacoes
{
  "valor": 100.00,
  "tipoDoacao": "MONETARIA",
  "mensagem": "Doação para ajudar os animais",
  "nomeDoador": "João Silva",
  "emailDoador": "joao@email.com",
  "telefoneDoador": "11999999999",
  "beneficiarioId": "ong-id"
}

# Get donation by ID
GET /doacoes/{id}

# List donations with filters
GET /doacoes?beneficiarioId={id}&status=PENDENTE

# Get donations by beneficiary
GET /doacoes/beneficiario/{beneficiarioId}

# Get donation statistics
GET /doacoes/beneficiario/{beneficiarioId}/stats

# Process donation
PATCH /doacoes/{id}/processar?transactionId=TXN-123

# Mark as failed
PATCH /doacoes/{id}/falha

# Cancel donation
PATCH /doacoes/{id}/cancelar

# Get metadata
GET /doacoes/metadata/tipos
GET /doacoes/metadata/status
```

#### ONG Management

```bash
# List ONGs
GET /ongs?search=termo

# Get all ONGs (no pagination)
GET /ongs/all

# Get ONG by ID
GET /ongs/{id}

# Get ONG by CNPJ
GET /ongs/cnpj/{cnpj}

# Update ONG
PUT /ongs/{id}

# Deactivate ONG
DELETE /ongs/{id}

# Activate ONG
PATCH /ongs/{id}/activate

# Check CNPJ availability
GET /ongs/check-cnpj/{cnpj}
```

## Database Schema

### Tables Created

1. **pets** - Pet information
2. **doacoes** - Donation records
3. Enhanced **ongs**, **protetores**, **users** tables with new relationships

### Migration Files
- `V3__create_pets_and_donations_tables.sql` - Creates new tables with indexes and constraints

### Key Features
- Comprehensive indexing for performance
- Text search capabilities
- Referential integrity
- Check constraints for data validation
- Audit triggers for timestamp updates

## Performance Optimizations

### Database Indexes
- Composite indexes for common filter combinations
- Text search indexes using PostgreSQL's full-text search
- Partial indexes for active records only
- Statistics collection for query optimization

### Caching Strategy
- **Caffeine Cache** for high-performance in-memory caching
- Separate cache configurations for production and testing
- Cache eviction strategies for data consistency
- TTL-based cache expiration

### Cache Services
- `PetCacheService` - Manages pet-related caches
- `DonationCacheService` - Manages donation-related caches
- Automatic cache invalidation on data changes

## Testing Strategy

### Unit Tests
- Service layer testing with Mockito
- Complete coverage of business logic
- Error scenario testing
- Validation testing

### Integration Tests
- End-to-end API testing
- Authentication testing
- Error response validation
- JSON serialization/deserialization

### Performance Tests
- Search performance benchmarks
- Concurrent access testing
- Large dataset handling
- Query optimization validation

## Security Features

### Input Validation
- DTO validation with Bean Validation
- Email format validation
- CNPJ/CPF validation
- Business rule validation

### Error Handling
- Comprehensive error codes
- Security-sensitive error filtering
- Proper HTTP status codes
- Detailed logging

### Authentication & Authorization
- JWT-based authentication
- Role-based access control
- Resource ownership validation
- API endpoint protection

## Frontend Integration Support

### CORS Configuration
- Environment-specific CORS settings
- Proper headers for frontend communication
- Security-conscious configuration

### API Documentation
- Swagger/OpenAPI integration
- Comprehensive endpoint documentation
- Example requests/responses
- Error code documentation

### Response Optimization
- Optimized DTOs for different use cases
- Pagination support
- Filtering and search capabilities
- Metadata endpoints for dropdowns

## Development Guidelines

### Code Quality
- Comprehensive logging
- Exception handling
- Code documentation
- Clean architecture principles

### Performance Best Practices
- Lazy loading for relationships
- Efficient queries with projections
- Connection pooling
- Query result caching

### Scalability Considerations
- Stateless service design
- Database connection optimization
- Cache-first architecture
- Horizontal scaling ready

## Deployment Instructions

### Database Setup
1. Run Flyway migrations
2. Configure database connection
3. Set up indexes and constraints
4. Verify schema creation

### Application Configuration
1. Configure cache settings
2. Set up JWT secrets
3. Configure CORS origins
4. Set logging levels

### Performance Tuning
1. Monitor cache hit rates
2. Analyze query performance
3. Adjust cache sizes
4. Optimize database connections

## Monitoring and Maintenance

### Metrics to Monitor
- API response times
- Cache hit/miss ratios
- Database query performance
- Error rates and patterns

### Maintenance Tasks
- Cache cleanup
- Database maintenance
- Log rotation
- Performance analysis

## Error Handling Reference

### Pet Errors
- `PET001` - Pet not found
- `PET002` - Pet already adopted
- `PET003` - Pet not available

### Donation Errors
- `DON001` - Donation not found
- `DON002` - Invalid amount
- `DON003` - Already processed

### User Errors
- `USER001` - User not found
- `USER005` - User inactive
- `AUTH008` - Access denied

## Production Checklist

- [ ] Database migrations applied
- [ ] Cache configuration optimized
- [ ] Security settings configured
- [ ] Monitoring set up
- [ ] Error handling tested
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Integration tests passing

## Future Enhancements

1. **Image Upload Service** - For pet photos
2. **Notification System** - For adoption updates
3. **Reporting Dashboard** - Analytics and insights
4. **Mobile API Optimization** - Specific endpoints for mobile
5. **Advanced Search** - Geolocation-based filtering
6. **Payment Integration** - Real payment processing
7. **Admin Panel** - Content management system

This implementation provides a robust, scalable foundation for the pet adoption platform with comprehensive features for managing pets, donations, and organizations.