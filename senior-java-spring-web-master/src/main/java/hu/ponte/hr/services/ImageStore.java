package hu.ponte.hr.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageStore {

    @Value("${hu.ponte.upload.directory:uploads}")
    private String rootOfUploadDirectory;

    private final Logger LOG = LoggerFactory.getLogger(ImageStore.class);
    private final String SEPARATOR = FileSystems.getDefault().getSeparator();


    @PostConstruct
    public void init() {
        try {
            LOG.info("Upload directory: " + Paths.get(rootOfUploadDirectory).toAbsolutePath());
            Path root = Paths.get(rootOfUploadDirectory).toAbsolutePath();
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Nem jött létre a root könyvtár: " + e);
        }
    }

    public void save(MultipartFile multipartFile, Long id) {
        try (InputStream file = multipartFile.getInputStream()) {
            Path path = Paths.get(rootOfUploadDirectory, SEPARATOR, String.valueOf(id)).toAbsolutePath();
            Files.createDirectory(path);
            Path filepath = Paths.get(path.toString(), multipartFile.getOriginalFilename());
            copyFile(file, filepath);
        } catch (IOException e) {
            throw new RuntimeException("Hiba a feltöltés közben: " + e);
        }
    }

    private void copyFile(InputStream inputStream, Path path) {
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Másolási hiba: " + e);
        }
    }

}
