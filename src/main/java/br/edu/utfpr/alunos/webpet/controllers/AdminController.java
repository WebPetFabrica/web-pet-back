package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.infra.openapi.ErrorExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints administrativos - acesso restrito para administradores")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    @GetMapping("/health")
    @Operation(
        summary = "Verificar saúde dos endpoints administrativos",
        description = "Endpoint de teste para verificar se os endpoints administrativos estão funcionando"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Endpoint administrativo funcionando corretamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Resposta de saúde",
                value = "\"Admin endpoint is working\""
            )
        )
    )
    @ErrorExamples.Unauthorized
    @ErrorExamples.Forbidden
    @ErrorExamples.InternalServerError
    public ResponseEntity<String> health() {
        String correlationId = MDC.get("correlationId");
        
        log.info("Admin health check requested [correlationId: {}]", correlationId);
        
        String response = "Admin endpoint is working";
        
        log.info("Admin health check completed successfully [correlationId: {}]", correlationId);
        
        return ResponseEntity.ok(response);
    }
}