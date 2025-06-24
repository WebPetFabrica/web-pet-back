package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.dto.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.services.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/donation")
@RequiredArgsConstructor
public class DonationController {
    private final DonationService donationService;

    @PostMapping("/donate")
    public ResponseEntity<DonationResponseDTO> donate(@RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String paymentMethod = body.get("paymentMethod").toString();
        DonationResponseDTO response = donationService.donate(amount, paymentMethod);
        return ResponseEntity.ok(response);
    }
}