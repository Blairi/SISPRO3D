package com.sispro3d.unam.core.storage;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Reglas de subida de entregables (modelos 3D y paquetes comprimidos) y
 * previsualizaciones (imágenes): extensión permitida, tamaño máximo y archivo
 * no vacío. Devuelve el mensaje de error (español, resuelto desde
 * {@code messages.properties}) o {@code null} si el archivo es aceptable.
 */
@Component
public class UploadValidator {

    public static final long MAX_BYTES = 25L * 1024 * 1024;
    public static final long MAX_ARCHIVE_BYTES = 100L * 1024 * 1024;

    private static final List<String> MODEL_EXTENSIONS = List.of("obj", "fbx", "stl", "blend");
    private static final List<String> ARCHIVE_EXTENSIONS = List.of("zip", "rar", "7z");
    private static final List<String> IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg", "webp");

    private final MessageSource messageSource;

    public UploadValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String deliverableError(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return message("upload.empty");
        }
        String extension = FileStorageService.extensionOf(file.getOriginalFilename());
        boolean archive = ARCHIVE_EXTENSIONS.contains(extension);
        if (!archive && !MODEL_EXTENSIONS.contains(extension)) {
            return message("upload.invalidType.deliverable");
        }
        if (archive) {
            return file.getSize() > MAX_ARCHIVE_BYTES ? message("upload.tooLarge.archive") : null;
        }
        return file.getSize() > MAX_BYTES ? message("upload.tooLarge") : null;
    }

    public String previewError(MultipartFile file) {
        return error(file, IMAGE_EXTENSIONS, "upload.invalidType.preview", MAX_BYTES);
    }

    private String error(MultipartFile file, List<String> allowed, String typeKey, long maxBytes) {
        if (file == null || file.isEmpty()) {
            return message("upload.empty");
        }
        if (!allowed.contains(FileStorageService.extensionOf(file.getOriginalFilename()))) {
            return message(typeKey);
        }
        if (file.getSize() > maxBytes) {
            return message("upload.tooLarge");
        }
        return null;
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
