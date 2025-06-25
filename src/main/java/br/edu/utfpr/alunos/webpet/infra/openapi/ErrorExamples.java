package br.edu.utfpr.alunos.webpet.infra.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ErrorExamples {
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Erro de validação",
                value = """
                {
                  "message": "Email já cadastrado",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface BadRequest {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "401",
        description = "Não autorizado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Credenciais inválidas",
                value = """
                {
                  "message": "Credenciais inválidas",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface Unauthorized {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "404",
        description = "Recurso não encontrado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Usuário não encontrado",
                value = """
                {
                  "message": "Usuário não encontrado",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface NotFound {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "403",
        description = "Acesso negado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Acesso negado",
                value = """
                {
                  "message": "Acesso negado para este recurso",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface Forbidden {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "429",
        description = "Muitas tentativas",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Conta bloqueada",
                value = """
                {
                  "message": "Conta temporariamente bloqueada",
                  "locked": true,
                  "lockoutDuration": "30 minutos",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface TooManyRequests {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "500",
        description = "Erro interno do servidor",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Erro interno",
                value = """
                {
                  "message": "Erro interno do servidor",
                  "timestamp": "2024-06-04T10:30:00"
                }
                """
            )
        )
    )
    @interface InternalServerError {}
    
    record ErrorResponse(String message, String timestamp) {}
}