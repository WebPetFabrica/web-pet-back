package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.Donation;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.UserDTO;
import br.edu.utfpr.alunos.webpet.repositories.DonationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DonationService {
    private final DonationRepository donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public DonationResponseDTO donate(BigDecimal amount, String paymentMethod) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Donation donation = new Donation();
        donation.setDonor(user);
        donation.setAmount(amount);
        donation.setPaymentMethod(paymentMethod);
        donation.setDonationDate(LocalDateTime.now());
        donationRepository.save(donation);

        return new DonationResponseDTO(
                donation.getId(),
                new UserDTO(user.getId(), user.getName(), user.getEmail()),
                donation.getAmount(),
                donation.getPaymentMethod(),
                donation.getDonationDate()
        );
    }
}