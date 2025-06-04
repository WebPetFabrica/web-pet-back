package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.services.auth.AuthenticationService;
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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {
    
    private final AuthenticationService authenticationService;

    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário no sistema e retorna um token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                    {
                        "displayName": "João Silva",
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro de autenticação",
                    value = """
                    {
                        "message": "Credenciais inválidas",
                        "code": "AUTH_INVALID_CREDENTIALS",
                        "timestamp": "2025-06-04T10:30:00Z",
                        "path": "/auth/login"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Muitas tentativas de login",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conta bloqueada",
                    value = """
                    {
                        "message": "Conta temporariamente bloqueada devido a muitas tentativas de login",
                        "code": "AUTH_ACCOUNT_LOCKED",
                        "timestamp": "2025-06-04T10:30:00Z",
                        "path": "/auth/login"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados de login do usuário",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Login exemplo",
                        value = """
                        {
                            "email": "usuario@exemplo.com",
                            "password": "minhasenha123"
                        }
                        """
                    )
                )
            ) LoginRequestDTO loginDTO) {
        
        String correlationId = MDC.get("correlationId");
        log.info("Login request received for email: {} [correlationId: {}]", 
            loginDTO.email(), correlationId);
        
        AuthResponseDTO response = authenticationService.login(loginDTO);
        
        log.info("Login successful for email: {} [correlationId: {}]", 
            loginDTO.email(), correlationId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Registrar usuário comum",
        description = "Registra um novo usuário comum no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuário registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou email já existe",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados para registro de usuário comum",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Registro usuário",
                        value = """
                        {
                            "name": "João Silva",
                            "email": "joao@exemplo.com",
                            "password": "minhasenha123"
                        }
                        """
                    )
                )
            ) RegisterRequestDTO registerDTO) {
        
        String correlationId = MDC.get("correlationId");
        log.info("User registration request received for email: {} [correlationId: {}]", 
            registerDTO.email(), correlationId);
        
        AuthResponseDTO response = authenticationService.registerUser(registerDTO);
        
        log.info("User registration successful for email: {} [correlationId: {}]", 
            registerDTO.email(), correlationId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Registrar ONG",
        description = "Registra uma nova ONG no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "ONG registrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos, email ou CNPJ já existe",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register/ong")
    public ResponseEntity<AuthResponseDTO> registerONG(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados para registro de ONG",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Registro ONG",
                        value = """
                        {
                            "nomeOng": "ONG Amigos dos Animais",
                            "cnpj": "12.345.678/0001-90",
                            "email": "contato@ongamigos.org",
                            "password": "senhaong123",
                            "celular": "(11) 99999-9999"
                        }
                        """
                    )
                )
            ) ONGRegisterDTO ongDTO) {
        
        String correlationId = MDC.get("correlationId");
        log.info("ONG registration request received for email: {} [correlationId: {}]", 
            ongDTO.email(), correlationId);
        
        AuthResponseDTO response = authenticationService.registerONG(ongDTO);
        
        log.info("ONG registration successful for email: {} [correlationId: {}]", 
            ongDTO.email(), correlationId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Registrar protetor",
        description = "Registra um novo protetor independente no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Protetor registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos, email ou CPF já existe",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register/protetor")
    public ResponseEntity<AuthResponseDTO> registerProtetor(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados para registro de protetor",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Registro protetor",
                        value = """
                        {
                            "nomeCompleto": "Maria dos Santos",
                            "cpf": "123.456.789-00",
                            "email": "maria@exemplo.com",
                            "password": "senhaprotetor123",
                            "celular": "(11) 88888-8888"
                        }
                        """
                    )
                )
            ) ProtetorRegisterDTO protetorDTO) {
        
        String correlationId = MDC.get("correlationId");
        log.info("Protetor registration request received for email: {} [correlationId: {}]", 
            protetorDTO.email(), correlationId);
        
        AuthResponseDTO response = authenticationService.registerProtetor(protetorDTO);
        
        log.info("Protetor registration successful for email: {} [correlationId: {}]", 
            protetorDTO.email(), correlationId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Status de saúde da autenticação",
        description = "Verifica se o serviço de autenticação está funcionando"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Serviço funcionando normalmente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                    "status": "OK",
                    "message": "Authentication service is healthy",
                    "timestamp": "2025-06-04T10:30:00Z"
                }
                """
            )
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        String correlationId = MDC.get("correlationId");
        log.debug("Health check requested [correlationId: {}]", correlationId);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "status", "OK",
            "message", "Authentication service is healthy",
            "timestamp", java.time.Instant.now(),
            "correlationId", correlationId != null ? correlationId : "none"
        ));
    }
}
