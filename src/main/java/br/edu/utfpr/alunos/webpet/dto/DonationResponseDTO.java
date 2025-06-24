package br.edu.utfpr.alunos.webpet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationResponseDTO(
        String id,
        UserDTO donor,
        BigDecimal amount,
        String paymentMethod,
        LocalDateTime donationDate
) {}