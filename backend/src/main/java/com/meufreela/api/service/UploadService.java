package com.meufreela.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class UploadService {

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final int MAX_FOTOS_POR_SERVICO = 5;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    /**
     * Salva a foto de perfil do usuário e retorna o path público
     * (ex: /uploads/usuarios/{id}.jpg).
     */
    public String salvarFotoUsuario(String usuarioId, MultipartFile file) throws IOException {
        validarImagem(file);

        String extensao = extensaoDoTipo(file.getContentType());
        String nomeArquivo = usuarioId + extensao;

        Path pasta = Paths.get(UPLOAD_DIR, "usuarios");
        Files.createDirectories(pasta);
        Path destino = pasta.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        log.info("Foto salva: {}", destino);

        // cache-busting: adiciona timestamp para o cliente não usar versão antiga
        return "/uploads/usuarios/" + nomeArquivo
                + "?v=" + System.currentTimeMillis();
    }

    public String salvarFotoServico(String servicoId, MultipartFile file) throws IOException {
        validarImagem(file);

        String extensao = extensaoDoTipo(file.getContentType());
        // UUID garante nome único — sem colisão mesmo com uploads concorrentes
        String nomeArquivo = "servico_" + servicoId + "_"
                + java.util.UUID.randomUUID() + extensao;

        Path pasta = Paths.get(UPLOAD_DIR, "servicos");
        Files.createDirectories(pasta);
        Path destino = pasta.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        log.info("Foto de serviço salva: {}", destino);

        return "/uploads/servicos/" + nomeArquivo;
    }
    private void validarImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Imagem muito grande (máx. 5MB)");
        }
        if (!TIPOS_PERMITIDOS.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Formato inválido. Use JPG, PNG ou WEBP");
        }
    }

    private String extensaoDoTipo(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}