package br.edu.utfpr.alunos.webpet.services.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of PaymentGatewayService for development and testing purposes.
 * 
 * <p>This implementation simulates payment processing without integrating
 * with a real payment gateway. It always returns true to simulate successful payments.
 * 
 */
@Slf4j
@Service
public class PaymentGatewayMockServiceImpl implements PaymentGatewayService {
    
    @Override
    public boolean processPayment(BigDecimal amount) {
        log.info("Processando pagamento mock no valor de {}", amount);
        
        // Simulate payment processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment processing interrupted", e);
        }
        
        // Mock always returns success
        return true;
    }
}