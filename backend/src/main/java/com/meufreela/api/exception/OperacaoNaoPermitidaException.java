package com.meufreela.api.exception;

/**
 * Lançada quando o usuário autenticado tenta operar sobre um recurso
 * que pertence a outro usuário.
 *
 * Herda de IllegalArgumentException para manter compatibilidade com
 * o contrato dos testes unitários existentes (ServicoServiceTest usa
 * isInstanceOf(IllegalArgumentException.class) nas checagens de dono).
 * O @ControllerAdvice mapeia esta subclasse especificamente para 409.
 */
public class OperacaoNaoPermitidaException extends IllegalArgumentException {
    public OperacaoNaoPermitidaException(String message) {
        super(message);
    }
}