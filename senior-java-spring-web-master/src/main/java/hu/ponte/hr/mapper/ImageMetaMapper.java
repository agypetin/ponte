package hu.ponte.hr.mapper;

import hu.ponte.hr.controller.ImageMeta;
import hu.ponte.hr.model.ImageMetaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMetaMapper {

    ImageMeta toDto(ImageMetaEntity entity);

    List<ImageMeta> toDto(List<ImageMetaEntity> entity);

    @Mapping(target = "name", expression = "java(file.getOriginalFilename())")
    @Mapping(target = "digitalSign", source = "sign")
    @Mapping(target = "size", expression = "java(file.getSize())")
    @Mapping(target = "mimeType", expression = "java(file.getContentType())")
    ImageMetaEntity toEntity(MultipartFile file, String sign);
}
