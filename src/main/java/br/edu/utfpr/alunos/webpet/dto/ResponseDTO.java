package br.edu.utfpr.alunos.webpet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String correlationId;

    public static ResponseDTO success(String message) {
        return ResponseDTO.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ResponseDTO success(String message, Object data) {
        return ResponseDTO.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ResponseDTO error(String message) {
        return ResponseDTO.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ResponseDTO error(String message, Object data) {
        return ResponseDTO.builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}