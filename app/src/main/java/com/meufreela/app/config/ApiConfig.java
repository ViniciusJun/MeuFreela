package com.meufreela.app.config;

public class ApiConfig {
    // Em produção: SEMPRE HTTPS + API Gateway
    // Para testes locais no emulador, 10.0.2.2 = localhost da sua máquina
    public static final String BASE_URL = "https://api.meufreela.com/v1/";
    public static final String BASE_URL_DEV = "http://10.0.2.2:8080/v1/";

    public static final int TIMEOUT_SEGUNDOS = 30;
}