package hu.ponte.hr.services;

import hu.ponte.hr.controller.ImageMeta;
import hu.ponte.hr.mapper.ImageMetaMapper;
import hu.ponte.hr.model.ImageMetaEntity;
import hu.ponte.hr.repository.ImageMetaRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ImageService {

    @Value("${hu.ponte.upload.directory:uploads}")
    private String rootOfUploadDirectory;

    private final String SEPARATOR = FileSystems.getDefault().getSeparator();

    private final ImageMetaRepository imageMetaRepository;
    private final ImageMetaMapper imageMetaMapper;
    private final ImageStore imageStore;
    private final SignService signService;

    public ImageService(ImageMetaRepository imageMetaRepository, ImageMetaMapper imageMetaMapper, ImageStore imageStore, SignService signService) {
        this.imageMetaRepository = imageMetaRepository;
        this.imageMetaMapper = imageMetaMapper;
        this.imageStore = imageStore;
        this.signService = signService;
    }

    public void save(MultipartFile file) {
        try {
            String sign = signService.sign(file.getBytes());
            if (sign != null) {
                ImageMetaEntity entity = imageMetaRepository.save(imageMetaMapper.toEntity(file, sign));
                imageStore.save(file, entity.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("File mentési hiba: ", e);
        }
    }

    public List<ImageMeta> getAllImages() {
        return imageMetaMapper.toDto(imageMetaRepository.findAll());
    }

    public void getImage(String id, HttpServletResponse response) {
        ImageMetaEntity imageMetaEntity = imageMetaRepository.findById(Long.parseLong(id)).orElse(null);
        if (imageMetaEntity != null) {
            try (OutputStream os = response.getOutputStream()) {
                Path absolutePath = Paths.get(rootOfUploadDirectory + SEPARATOR + imageMetaEntity.getId() + SEPARATOR + imageMetaEntity.getName()).toAbsolutePath();
                InputStream in = new FileInputStream(absolutePath.toString());
                byte[] byteArray = IOUtils.toByteArray(in);
                response.setHeader("Content-Type", imageMetaEntity.getMimeType());
                response.setHeader("Content-Length", String.valueOf(imageMetaEntity.getSize()));
                response.setHeader("Content-Disposition", "inline; filename=\"" + imageMetaEntity.getName() + "\"");
                response.setContentLength(byteArray.length);
                os.write(byteArray, 0, byteArray.length);
            } catch (Exception e) {
                throw new RuntimeException("File olvasási hiba: ", e);
            }
        }
    }
}
