package com.sispro3d.unam.core.storage;

import com.sispro3d.unam.security.CurrentUser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Sirve los archivos almacenados bajo {@code /files/**} solo al cliente o al
 * experto de la orden propietaria; cualquier otro caso responde 404.
 */
@Controller
public class FileController {

    private final StoredFileAccessService storedFileAccessService;
    private final CurrentUser currentUser;

    public FileController(StoredFileAccessService storedFileAccessService, CurrentUser currentUser) {
        this.storedFileAccessService = storedFileAccessService;
        this.currentUser = currentUser;
    }

    @GetMapping("/files/{*reference}")
    public ResponseEntity<Resource> file(@PathVariable String reference) {
        String cleaned = reference.startsWith("/") ? reference.substring(1) : reference;

        return storedFileAccessService.resolveForUser(cleaned, currentUser.id())
                .<ResponseEntity<Resource>>map(path -> {
                    Resource resource = new FileSystemResource(path);
                    MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);
                    return ResponseEntity.ok().contentType(mediaType).body(resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
