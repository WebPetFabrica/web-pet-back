package br.edu.utfpr.alunos.webpet.dto;

import java.util.Map;

public record ResponseDTO(
    boolean success,
    String message,
    Map<String, Object> data
) {
    // Constructor for success responses with data
    public static ResponseDTO success(String message, Map<String, Object> data) {
        return new ResponseDTO(true, message, data);
    }

    // Constructor for error responses
    public static ResponseDTO error(String message) {
        return new ResponseDTO(false, message, null);
    }
}
