package com.sispro3d.unam.core.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UploadValidatorTest {

    private UploadValidator validator;

    @BeforeEach
    void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        validator = new UploadValidator(messageSource);
    }

    private MultipartFile file(String filename, long size, boolean empty) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getSize()).thenReturn(size);
        when(file.isEmpty()).thenReturn(empty);
        return file;
    }

    @Test
    void modelWithinLimit_isAccepted() {
        assertThat(validator.deliverableError(file("modelo.obj", 10L * 1024 * 1024, false))).isNull();
    }

    @Test
    void modelOverLimit_isRejected() {
        assertThat(validator.deliverableError(file("modelo.fbx", UploadValidator.MAX_BYTES + 1, false)))
                .isEqualTo("upload.tooLarge");
    }

    @Test
    void archiveLargerThanModelLimit_isAccepted() {
        assertThat(validator.deliverableError(file("paquete.zip", 30L * 1024 * 1024, false))).isNull();
    }

    @Test
    void archiveUpToArchiveLimit_isAccepted() {
        assertThat(validator.deliverableError(file("paquete.rar", UploadValidator.MAX_ARCHIVE_BYTES, false))).isNull();
        assertThat(validator.deliverableError(file("paquete.7z", UploadValidator.MAX_ARCHIVE_BYTES, false))).isNull();
    }

    @Test
    void archiveOverArchiveLimit_isRejected() {
        assertThat(validator.deliverableError(file("paquete.zip", UploadValidator.MAX_ARCHIVE_BYTES + 1, false)))
                .isEqualTo("upload.tooLarge.archive");
    }

    @Test
    void disallowedDeliverableType_isRejected() {
        assertThat(validator.deliverableError(file("malware.exe", 1024, false)))
                .isEqualTo("upload.invalidType.deliverable");
    }

    @Test
    void archiveTypeIsNotAcceptedAsPreview() {
        assertThat(validator.previewError(file("imagen.zip", 1024, false)))
                .isEqualTo("upload.invalidType.preview");
    }

    @Test
    void emptyFile_isRejected() {
        assertThat(validator.deliverableError(file("modelo.obj", 0, true)))
                .isEqualTo("upload.empty");
    }
}
