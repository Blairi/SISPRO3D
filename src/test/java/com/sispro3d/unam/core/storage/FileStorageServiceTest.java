package com.sispro3d.unam.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private final MockMultipartFile file = new MockMultipartFile(
            "file", "modelo.FBX", "application/octet-stream", "contenido".getBytes());

    @Test
    void store_writesFileAndReturnsRelativePath() {
        var storage = new FileStorageService(tempDir.toString());

        String reference = storage.store(file, "deliverables");

        assertThat(reference).startsWith("deliverables/").endsWith(".fbx");
        assertThat(Files.exists(storage.resolve(reference))).isTrue();
    }

    @Test
    void resolve_rejectsPathTraversal() {
        var storage = new FileStorageService(tempDir.toString());

        assertThatThrownBy(() -> storage.resolve("../secret.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_removesStoredFile_andIgnoresExternalUrl() {
        var storage = new FileStorageService(tempDir.toString());
        String reference = storage.store(file, "previews");

        storage.delete(reference);

        assertThat(Files.exists(storage.resolve(reference))).isFalse();
        storage.delete("https://files.render3d.mx/external.fbx");
    }

    @Test
    void isStoredFile_distinguishesLocalPathsFromExternalUrls() {
        assertThat(FileStorageService.isStoredFile("deliverables/abc.obj")).isTrue();
        assertThat(FileStorageService.isStoredFile("https://x/y.obj")).isFalse();
        assertThat(FileStorageService.isStoredFile(null)).isFalse();
        assertThat(FileStorageService.isStoredFile("")).isFalse();
    }
}
