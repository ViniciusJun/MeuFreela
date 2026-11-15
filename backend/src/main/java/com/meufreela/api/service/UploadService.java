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
    // CORREÇÃO: Declaração da constante que estava faltando
    private static final String PRIVATE_UPLOAD_DIR = "private_uploads";

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    /**
     * Salva a foto de perfil do usuário e retorna o path público.
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

        return "/uploads/usuarios/" + nomeArquivo + "?v=" + System.currentTimeMillis();
    }

    public String salvarFotoServico(String servicoId, MultipartFile file) throws IOException {
        validarImagem(file);

        String extensao = extensaoDoTipo(file.getContentType());
        String nomeArquivo = "servico_" + servicoId + "_" + UUID.randomUUID() + extensao;

        Path pasta = Paths.get(UPLOAD_DIR, "servicos");
        Files.createDirectories(pasta);
        Path destino = pasta.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        log.info("Foto de serviço salva: {}", destino);

        return "/uploads/servicos/" + nomeArquivo;
    }

    /**
     * Salva documentos de KYC em uma pasta privada.
     * Retorna o nome do arquivo (não uma URL pública).
     */
    public String salvarDocumentoKyc(String usuarioId, MultipartFile file, String tipo) throws IOException {
        validarImagem(file);

        String extensao = extensaoDoTipo(file.getContentType());
        String nomeArquivo = tipo + "_" + usuarioId + "_" + UUID.randomUUID() + extensao;

        Path pasta = Paths.get(PRIVATE_UPLOAD_DIR, "kyc");
        Files.createDirectories(pasta);
        Path destino = pasta.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        log.info("Documento KYC salvo: {}", destino);

        return nomeArquivo;
    }

    private void validarImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Imagem muito grande (máx. 5MB)");
        }
        if (!TIPOS_PERMITIDOS.contains(file.getContentType())) {
            throw new IllegalArgumentException("Formato inválido. Use JPG, PNG ou WEBP");
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