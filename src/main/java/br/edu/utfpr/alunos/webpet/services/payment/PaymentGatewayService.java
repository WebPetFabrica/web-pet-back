package br.edu.utfpr.alunos.webpet.services.payment;

import java.math.BigDecimal;

/**
 * Service interface for payment processing operations.
 * 
 * <p>This interface defines the contract for payment gateway integrations,
 * allowing for different implementations (mock, real gateway integrations, etc.).
 * 
 */
public interface PaymentGatewayService {
    
    /**
     * Processes a payment for the specified amount.
     * 
     * @param amount the amount to be processed
     * @return true if payment was successful, false otherwise
     */
    boolean processPayment(BigDecimal amount);
}