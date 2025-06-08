package br.edu.utfpr.alunos.webpet.dto.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
public record DonationResponseDTO(
    String id,
    BigDecimal valor,
    TipoDoacao tipoDoacao,
    String mensagem,
    String nomeDoador,
    String emailDoador,
    String telefoneDoador,
    StatusDoacao statusDoacao,
    String transactionId,
    String beneficiarioId,
    LocalDateTime criadoEm,
    LocalDateTime processadoEm
) {}