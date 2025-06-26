package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.openapi.ErrorExamples;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Endpoints para gerenciamento de perfil do usuário")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @Operation(
        summary = "Obter perfil atual",
        description = "Retorna o perfil do usuário autenticado"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Perfil obtido com sucesso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserResponseDTO.class),
            examples = @ExampleObject(
                name = "Perfil do usuário",
                value = """
                {
                  "id": "uuid-123",
                  "email": "joao@exemplo.com",
                  "displayName": "João Silva",
                  "userType": "USER",
                  "identifier": "joao@exemplo.com",
                  "active": true
                }
                """
            )
        )
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.NotFound
    @ErrorExamples.InternalServerError
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String correlationId = MDC.get("correlationId");
        String userEmail = authentication.getName();
        
        log.info("Getting current user profile for: {} [correlationId: {}]", userEmail, correlationId);
        
        UserResponseDTO user = userService.getCurrentUserProfile(userEmail);
        
        log.info("Current user profile retrieved successfully for: {} [correlationId: {}]", 
            userEmail, correlationId);
        
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obter usuário por ID",
        description = "Retorna o perfil de um usuário específico pelo ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Usuário encontrado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserResponseDTO.class),
            examples = @ExampleObject(
                name = "Usuário encontrado",
                value = """
                {
                  "id": "uuid-456",
                  "email": "maria@ong.com",
                  "displayName": "ONG Amigos dos Animais",
                  "userType": "ONG",
                  "identifier": "12345678000190",
                  "active": true
                }
                """
            )
        )
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.NotFound
    @ErrorExamples.InternalServerError
    public ResponseEntity<UserResponseDTO> getUserById(
        @Parameter(description = "ID do usuário", example = "uuid-123")
        @PathVariable String id
    ) {
        String correlationId = MDC.get("correlationId");
        String currentUser = getCurrentUserEmail();
        
        log.info("Getting user by ID: {} requested by: {} [correlationId: {}]", 
            id, currentUser, correlationId);
        
        UserResponseDTO user = userService.getUserById(id);
        
        log.info("User retrieved successfully by ID: {} [correlationId: {}]", id, correlationId);
        
        return ResponseEntity.ok(user);
    }
    
    @PatchMapping("/deactivate")
    @Operation(
        summary = "Desativar conta",
        description = "Desativa a conta do usuário autenticado"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Conta desativada com sucesso"
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.NotFound
    @ErrorExamples.InternalServerError
    public ResponseEntity<Void> deactivateCurrentUser(Authentication authentication) {
        String correlationId = MDC.get("correlationId");
        String userEmail = authentication.getName();
        
        log.info("Deactivating account for user: {} [correlationId: {}]", userEmail, correlationId);
        
        userService.deactivateUser(userEmail);
        
        log.info("Account deactivated successfully for user: {} [correlationId: {}]", 
            userEmail, correlationId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/activate")
    @Operation(
        summary = "Ativar conta",
        description = "Reativa a conta do usuário autenticado"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Conta ativada com sucesso"
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.NotFound
    @ErrorExamples.InternalServerError
    public ResponseEntity<Void> activateCurrentUser(Authentication authentication) {
        String correlationId = MDC.get("correlationId");
        String userEmail = authentication.getName();
        
        log.info("Activating account for user: {} [correlationId: {}]", userEmail, correlationId);
        
        userService.activateUser(userEmail);
        
        log.info("Account activated successfully for user: {} [correlationId: {}]", 
            userEmail, correlationId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/status")
    @Operation(
        summary = "Verificar status da conta",
        description = "Verifica se a conta do usuário autenticado está ativa"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Status da conta",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Status da conta",
                value = """
                {
                  "active": true,
                  "email": "joao@exemplo.com",
                  "lastCheck": "2025-06-04T10:30:00Z"
                }
                """
            )
        )
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.InternalServerError
    public ResponseEntity<Object> getAccountStatus(Authentication authentication) {
        String correlationId = MDC.get("correlationId");
        String userEmail = authentication.getName();
        
        log.debug("Checking account status for user: {} [correlationId: {}]", userEmail, correlationId);
        
        boolean isActive = userService.isUserActive(userEmail);
        
        Object response = java.util.Map.of(
            "active", isActive,
            "email", userEmail,
            "lastCheck", java.time.Instant.now(),
            "correlationId", correlationId != null ? correlationId : "none"
        );
        
        log.debug("Account status checked for user: {} - active: {} [correlationId: {}]", 
            userEmail, isActive, correlationId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile/detailed")
    @Operation(
        summary = "Obter perfil detalhado",
        description = "Retorna o perfil detalhado do usuário autenticado com informações específicas do tipo"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Perfil detalhado obtido com sucesso",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Perfil detalhado",
                value = """
                {
                  "id": "uuid-123",
                  "email": "joao@exemplo.com",
                  "displayName": "João Silva",
                  "userType": "USER",
                  "identifier": "joao@exemplo.com",
                  "active": true,
                  "createdAt": "2025-01-01T10:00:00Z",
                  "lastLogin": "2025-06-04T09:30:00Z"
                }
                """
            )
        )
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.NotFound
    @ErrorExamples.InternalServerError
    public ResponseEntity<Object> getDetailedProfile(Authentication authentication) {
        String correlationId = MDC.get("correlationId");
        String userEmail = authentication.getName();
        
        log.info("Getting detailed profile for user: {} [correlationId: {}]", userEmail, correlationId);
        
        Object detailedProfile = userService.getUserDetailedProfile(userEmail);
        
        log.info("Detailed profile retrieved successfully for user: {} [correlationId: {}]", 
            userEmail, correlationId);
        
        return ResponseEntity.ok(detailedProfile);
    }
    
    private String getCurrentUserEmail() {
        return MDC.get("userId") != null ? MDC.get("userId") : "anonymous";
    }
    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<UserResponseDTO> getCurrentUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        UserResponseDTO userProfile = userService.getCurrentUserProfile(userEmail);
        return ResponseEntity.ok(userProfile);
    }

}