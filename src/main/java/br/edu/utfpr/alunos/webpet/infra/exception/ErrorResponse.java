package br.edu.utfpr.alunos.webpet.infra.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record ErrorResponse(
    String code,
    String message,
    String path,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    Map<String, List<String>> fieldErrors
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String path, Map<String, List<String>> fieldErrors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
    }
    
    public ErrorResponse withAdditionalInfo(String key, String value) {
        return ErrorResponse.builder()
                .code(this.code)
                .message(this.message)
                .path(this.path)
                .timestamp(this.timestamp)
                .fieldErrors(this.fieldErrors)
                .build();
    }
}