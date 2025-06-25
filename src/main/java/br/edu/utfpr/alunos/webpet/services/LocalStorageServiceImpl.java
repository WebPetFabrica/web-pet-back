package br.edu.utfpr.alunos.webpet.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements FileStorageService {
    private static final String PET_IMAGES_PATH = "/images/pets/";

    @Value("${file.upload-dir:./uploads/pets}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // Validar se o arquivo está vazio
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao armazenar arquivo vazio");
            }

            // Criar diretório se não existir
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Gerar nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            fileExtension = fileExtension.toLowerCase();
            if (!fileExtension.matches("\\.(jpg < /dev/null | jpeg|png)")) {
                throw new RuntimeException("Extensão de arquivo inválida: " + fileExtension);
            }
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Salvar o arquivo
            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retornar o caminho relativo da imagem
            return PET_IMAGES_PATH + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar arquivo: " + e.getMessage());
        }
    }
}