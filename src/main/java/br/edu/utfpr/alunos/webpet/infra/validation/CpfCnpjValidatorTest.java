package br.edu.utfpr.alunos.webpet.infra.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

class CpfCnpjValidatorTest {
    
    private CpfValidator cpfValidator;
    private CnpjValidator cnpjValidator;
    private ConstraintValidatorContext context;
    
    @BeforeEach
    void setUp() {
        cpfValidator = new CpfValidator();
        cnpjValidator = new CnpjValidator();
        // Mock context não é necessário para estes testes
    }
    
    @Test
    void shouldValidateCpfCorrectly() {
        assertThat(cpfValidator.isValid("11144477735", context)).isTrue();
        assertThat(cpfValidator.isValid("111.444.777-35", context)).isTrue();
        assertThat(cpfValidator.isValid("12345678909", context)).isTrue();
    }
    
    @Test
    void shouldRejectInvalidCpf() {
        assertThat(cpfValidator.isValid("11111111111", context)).isFalse(); // Sequência
        assertThat(cpfValidator.isValid("12345678901", context)).isFalse(); // Inválido
        assertThat(cpfValidator.isValid("123", context)).isFalse(); // Muito curto
        assertThat(cpfValidator.isValid(null, context)).isFalse(); // Null
    }
    
    @Test
    void shouldValidateCnpjCorrectly() {
        assertThat(cnpjValidator.isValid("11222333000181", context)).isTrue();
        assertThat(cnpjValidator.isValid("11.222.333/0001-81", context)).isTrue();
    }
    
    @Test
    void shouldRejectInvalidCnpj() {
        assertThat(cnpjValidator.isValid("11111111111111", context)).isFalse(); // Sequência
        assertThat(cnpjValidator.isValid("12345678000190", context)).isFalse(); // Inválido
        assertThat(cnpjValidator.isValid("123", context)).isFalse(); // Muito curto
        assertThat(cnpjValidator.isValid(null, context)).isFalse(); // Null
    }
}