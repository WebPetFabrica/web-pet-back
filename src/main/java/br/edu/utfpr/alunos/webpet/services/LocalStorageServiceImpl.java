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
    private static final String ALLOWED_FILE_EXTENSIONS_REGEX = "\\.(jpg|jpeg|png)$";

    @Value("${file.upload-dir:./uploads/pets}")
    private String uploadDir;
    
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // Validar se o arquivo está vazio
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao armazenar arquivo vazio");
            }
            
            // Valida o tamanho do ficheiro
            long maxSizeInBytes = parseSize(maxFileSize);
            if (file.getSize() > maxSizeInBytes) {
                throw new RuntimeException("Ficheiro excede o tamanho máximo permitido de " + maxFileSize);
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
                if (!fileExtension.matches(ALLOWED_FILE_EXTENSIONS_REGEX)) {
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
    
    private long parseSize(String size) {
        size = size.toUpperCase();
        if (size.endsWith("MB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
        }
        return Long.parseLong(size);
    }
}