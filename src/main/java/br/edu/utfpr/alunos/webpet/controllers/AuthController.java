package br.edu.utfpr.alunos.webpet.controllers;

// IMPORTS - Combinando main + feature
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository; // FROM MAIN
import br.edu.utfpr.alunos.webpet.repositories.UserRepository; // FROM MAIN
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository; // FROM FEATURE

import br.edu.utfpr.alunos.webpet.services.auth.AuthenticationService; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.services.auth.UserRegistrationService; // NEW
import br.edu.utfpr.alunos.webpet.services.validation.PasswordHistoryService; // FROM FEATURE
// import br.edu.utfpr.alunos.webpet.services.validation.EmailConfirmationService; // FROM FEATURE - NOT IMPLEMENTED YET

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO; // FROM FEATURE
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO; // FROM FEATURE

// Swagger documentation
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {
    
    // DEPENDENCIES - Combinando main + feature
    private final UserRepository userRepository; // FROM MAIN
    private final AnimalRepository animalRepository; // FROM MAIN (se usado)
    private final ONGRepository ongRepository; // FROM FEATURE
    private final ProtetorRepository protetorRepository; // FROM FEATURE
    
    private final AuthenticationService authenticationService; // FROM FEATURE (novo)
    private final UserRegistrationService userRegistrationService; // NEW
    private final PasswordHistoryService passwordHistoryService; // FROM FEATURE
    // private final EmailConfirmationService emailConfirmationService; // FROM FEATURE - NOT IMPLEMENTED YET
    
    @PostMapping("/login")
    @Operation(
        summary = "Realiza login do usuário",
        description = "Autentica um usuário com email e senha, retornando um token JWT"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "423", description = "Conta temporariamente bloqueada")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("Login attempt for email: {} [correlationId: {}]", 
                loginRequest.email(), correlationId);
            
            // MERGED: Usar novo serviço com fallback para o antigo se necessário
            AuthResponseDTO response = authenticationService.authenticate(loginRequest);
            
            log.info("Login successful for email: {} [correlationId: {}]", 
                loginRequest.email(), correlationId);
            
            return ResponseEntity.ok(response);
            
        } finally {
            MDC.clear();
        }
    }
    
    @PostMapping("/register/user")
    @Operation(
        summary = "Registra um novo usuário comum",
        description = "Cria uma nova conta de usuário comum no sistema"
    )
    public ResponseEntity<AuthResponseDTO> registerUser(@Valid @RequestBody UserRegisterDTO registerRequest) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("User registration attempt for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            AuthResponseDTO response = userRegistrationService.registerUser(registerRequest);
            
            log.info("User registration successful for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } finally {
            MDC.clear();
        }
    }
    
    @PostMapping("/register/ong")
    @Operation(
        summary = "Registra uma nova ONG",
        description = "Cria uma nova conta de ONG no sistema"
    )
    public ResponseEntity<AuthResponseDTO> registerONG(@Valid @RequestBody ONGRegisterDTO registerRequest) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("ONG registration attempt for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            AuthResponseDTO response = userRegistrationService.registerONG(registerRequest);
            
            log.info("ONG registration successful for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } finally {
            MDC.clear();
        }
    }
    
    @PostMapping("/register/protetor")
    @Operation(
        summary = "Registra um novo protetor",
        description = "Cria uma nova conta de protetor independente no sistema"
    )
    public ResponseEntity<AuthResponseDTO> registerProtetor(@Valid @RequestBody ProtetorRegisterDTO registerRequest) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("Protetor registration attempt for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            AuthResponseDTO response = userRegistrationService.registerProtetor(registerRequest);
            
            log.info("Protetor registration successful for email: {} [correlationId: {}]", 
                registerRequest.email(), correlationId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } finally {
            MDC.clear();
        }
    }
    
    @GetMapping("/confirm")
    @Operation(
        summary = "Confirma email do usuário",
        description = "Confirma o email do usuário através do token enviado por email"
    )
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            log.info("Email confirmation attempt [correlationId: {}]", correlationId);
            
            // TODO: Implement EmailConfirmationService
            log.info("Email confirmation endpoint called - feature not yet implemented [correlationId: {}]", correlationId);
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Funcionalidade de confirmação de email ainda não implementada");
            
        } finally {
            MDC.clear();
        }
    }
    
    // COMPATIBILIDADE: Manter endpoints existentes do main se necessário
    @PostMapping("/legacy/login") // Exemplo de endpoint legado
    @Deprecated(since = "2.0.0", forRemoval = true)
    public ResponseEntity<?> legacyLogin(@RequestBody Object legacyRequest) {
        // Redirect to new authentication service or maintain compatibility
        log.warn("Using deprecated legacy login endpoint");
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .header("Location", "/api/auth/login")
            .body("Please use /api/auth/login endpoint");
    }
}