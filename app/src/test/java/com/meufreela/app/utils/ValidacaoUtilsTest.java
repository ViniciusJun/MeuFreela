package com.meufreela.app.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Testes unitários para a camada de validação local do app.
 * Rodam na JVM, sem emulador nem Android framework.
 */
public class ValidacaoUtilsTest {

    // ============================================================
    // EMAIL
    // ============================================================

    @Test
    public void emailValido_aceitaEmailSimples() {
        assertTrue(ValidacaoUtils.emailValido("joao@teste.com"));
    }

    @Test
    public void emailValido_aceitaEmailComPontoNoNome() {
        assertTrue(ValidacaoUtils.emailValido("joao.silva@empresa.com.br"));
    }

    @Test
    public void emailValido_rejeitaSemArroba() {
        assertFalse(ValidacaoUtils.emailValido("joaoteste.com"));
    }

    @Test
    public void emailValido_rejeitaSemDominio() {
        assertFalse(ValidacaoUtils.emailValido("joao@"));
    }

    @Test
    public void emailValido_rejeitaNulo() {
        assertFalse(ValidacaoUtils.emailValido(null));
    }

    @Test
    public void emailValido_rejeitaVazio() {
        assertFalse(ValidacaoUtils.emailValido(""));
    }

    // ============================================================
    // SENHA
    // ============================================================

    @Test
    public void senhaForte_aceitaSenhaComLetrasENumeros() {
        assertTrue(ValidacaoUtils.senhaForte("senha12345"));
    }

    @Test
    public void senhaForte_aceitaSenhaComMaiusculas() {
        assertTrue(ValidacaoUtils.senhaForte("SenhaForte2026"));
    }

    @Test
    public void senhaForte_rejeitaSenhaCurta() {
        assertFalse(ValidacaoUtils.senhaForte("abc12"));    // < 8 caracteres
    }

    @Test
    public void senhaForte_rejeitaApenasLetras() {
        assertFalse(ValidacaoUtils.senhaForte("senhasemnumero"));
    }

    @Test
    public void senhaForte_rejeitaApenasNumeros() {
        assertFalse(ValidacaoUtils.senhaForte("12345678"));
    }

    @Test
    public void senhaForte_rejeitaNulo() {
        assertFalse(ValidacaoUtils.senhaForte(null));
    }

    // ============================================================
    // CPF
    // ============================================================

    @Test
    public void cpfValido_aceitaCpfCom11Digitos() {
        assertTrue(ValidacaoUtils.cpfValido("12345678901"));
    }

    @Test
    public void cpfValido_aceitaCpfComFormatacao() {
        // O método limpa caracteres não-numéricos antes de validar
        assertTrue(ValidacaoUtils.cpfValido("123.456.789-01"));
    }

    @Test
    public void cpfValido_rejeitaCpfCurto() {
        assertFalse(ValidacaoUtils.cpfValido("123456"));
    }

    @Test
    public void cpfValido_rejeitaCpfComTodosDigitosIguais() {
        assertFalse(ValidacaoUtils.cpfValido("11111111111"));
        assertFalse(ValidacaoUtils.cpfValido("00000000000"));
    }

    @Test
    public void cpfValido_rejeitaNulo() {
        assertFalse(ValidacaoUtils.cpfValido(null));
    }

    // ============================================================
    // TELEFONE
    // ============================================================

    @Test
    public void telefoneValido_aceitaFixoCom10Digitos() {
        assertTrue(ValidacaoUtils.telefoneValido("1133334444"));
    }

    @Test
    public void telefoneValido_aceitaCelularCom11Digitos() {
        assertTrue(ValidacaoUtils.telefoneValido("11988887777"));
    }

    @Test
    public void telefoneValido_aceitaFormatado() {
        // O método limpa parênteses, espaços, hífens
        assertTrue(ValidacaoUtils.telefoneValido("(11) 98888-7777"));
    }

    @Test
    public void telefoneValido_rejeitaCurto() {
        assertFalse(ValidacaoUtils.telefoneValido("119888"));
    }

    @Test
    public void telefoneValido_rejeitaNulo() {
        assertFalse(ValidacaoUtils.telefoneValido(null));
    }
}