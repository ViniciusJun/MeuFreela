package com.meufreela.app.utils;

import android.util.Patterns;

public class ValidacaoUtils {

    public static boolean emailValido(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean senhaForte(String senha) {
        if (senha == null || senha.length() < 8) return false;
        boolean temLetra = senha.matches(".*[a-zA-Z].*");
        boolean temNumero = senha.matches(".*\\d.*");
        return temLetra && temNumero;
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null) return false;
        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        // Implementar validação de dígito verificador do CPF...
        return true;
    }

    public static boolean telefoneValido(String tel) {
        if (tel == null) return false;
        String numeros = tel.replaceAll("\\D", "");
        return numeros.length() >= 10 && numeros.length() <= 11;
    }
}