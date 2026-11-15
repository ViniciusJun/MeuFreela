package com.meufreela.app.utils;

public class ValidacaoUtils {

    public static boolean emailValido(String email) {
        if (email == null) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
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
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;
        return true;
    }

    public static boolean telefoneValido(String tel) {
        if (tel == null) return false;
        String numeros = tel.replaceAll("\\D", "");
        return numeros.length() >= 10 && numeros.length() <= 11;
    }
}