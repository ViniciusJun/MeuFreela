package com.meufreela.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Conflitos: e-mail ou CPF já cadastrado → 409 Conflict
     */
    @ExceptionHandler({EmailJaCadastradoException.class, CpfJaCadastradoException.class})
    public ResponseEntity<Map<String, Object>> handleConflito(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro(ex.getMessage(), 409));
    }

    /**
     * Credenciais inválidas → 401 Unauthorized
     */
    @ExceptionHandler({CredenciaisInvalidasException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleCredenciais(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(erro("E-mail ou senha inválidos", 401));
    }

    /**
     * IllegalArgumentException genérica → 409 Conflict (fallback)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro(ex.getMessage(), 409));
    }

    /**
     * Erros de validação (@Valid nos DTOs) → 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.put(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> corpo = erro("Dados inválidos", 400);
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    /**
     * Qualquer outra exceção não tratada → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        // ✅ Loga stacktrace completo no console do backend
        System.err.println("========================================");
        System.err.println("ERRO NÃO TRATADO: " + ex.getClass().getName());
        System.err.println("Mensagem: " + ex.getMessage());
        ex.printStackTrace(System.err);
        System.err.println("========================================");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(erro("Erro interno do servidor", 500));
    }

    private Map<String, Object> erro(String mensagem, int status) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status);
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}