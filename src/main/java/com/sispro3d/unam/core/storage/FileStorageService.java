package com.sispro3d.unam.core.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/**
 * Guarda los archivos subidos en una carpeta local configurable y devuelve una
 * referencia relativa (p. ej. {@code deliverables/7f3a….obj}) que se persiste en
 * la base y se sirve después bajo {@code /files/**}.
 */
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.storage.location:uploads}") String location) {
        this.root = Paths.get(location).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    public String store(MultipartFile file, String subfolder) {
        String extension = extensionOf(file.getOriginalFilename());
        String name = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        try {
            Path directory = root.resolve(subfolder).normalize();
            Files.createDirectories(directory);
            file.transferTo(directory.resolve(name));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar el archivo", ex);
        }
        return subfolder + "/" + name;
    }

    public void delete(String reference) {
        if (!isStoredFile(reference)) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(reference));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo borrar el archivo", ex);
        }
    }

    public Path resolve(String reference) {
        Path target = root.resolve(reference).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Ruta fuera del almacenamiento: " + reference);
        }
        return target;
    }

    public static boolean isStoredFile(String reference) {
        return reference != null && !reference.isBlank() && !reference.startsWith("http");
    }

    public static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
