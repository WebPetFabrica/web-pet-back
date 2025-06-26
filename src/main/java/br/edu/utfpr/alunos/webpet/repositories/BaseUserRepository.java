package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;

/**
 * Repository for finding BaseUser entities across all user types.
 * 
 * <p>This repository provides a unified way to find users regardless of their
 * specific type (User, ONG, or Protetor). It searches across all user tables
 * to find a user by ID.
 */
@Repository
public class BaseUserRepository {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ONGRepository ongRepository;
    
    @Autowired
    private ProtetorRepository protetorRepository;
    
    /**
     * Finds a BaseUser by ID across all user types.
     * 
     * @param id the user ID
     * @return the user if found, empty otherwise
     */
    public Optional<BaseUser> findById(String id) {
        // Try to find in User table first
        Optional<BaseUser> user = userRepository.findById(id).map(u -> (BaseUser) u);
        if (user.isPresent()) {
            return user;
        }
        
        // Try to find in ONG table
        Optional<BaseUser> ong = ongRepository.findById(id).map(o -> (BaseUser) o);
        if (ong.isPresent()) {
            return ong;
        }
        
        // Try to find in Protetor table
        return protetorRepository.findById(id).map(p -> (BaseUser) p);
    }
    
    /**
     * Finds an active BaseUser by ID across all user types.
     * 
     * @param id the user ID
     * @return the active user if found, empty otherwise
     */
    public Optional<BaseUser> findByIdAndActiveTrue(String id) {
        // Try to find in User table first
        Optional<BaseUser> user = userRepository.findByIdAndActiveTrue(id).map(u -> (BaseUser) u);
        if (user.isPresent()) {
            return user;
        }
        
        // Try to find in ONG table
        Optional<BaseUser> ong = ongRepository.findByIdAndActiveTrue(id).map(o -> (BaseUser) o);
        if (ong.isPresent()) {
            return ong;
        }
        
        // Try to find in Protetor table
        return protetorRepository.findByIdAndActiveTrue(id).map(p -> (BaseUser) p);
    }
}