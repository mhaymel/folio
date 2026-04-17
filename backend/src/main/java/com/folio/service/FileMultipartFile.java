package com.folio.service;

import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Adapts a filesystem {@link Path} to the {@link MultipartFile} interface
 * so that the {@link ImportService} methods can be called with local files.
 */
final class FileMultipartFile implements MultipartFile {

    private static final Logger LOG = getLogger(FileMultipartFile.class);

    private final Path path;

    FileMultipartFile(Path path) {
        this.path = requireNonNull(path);
    }

    @Override public String getName() { return path.getFileName().toString(); }
    @Override public String getOriginalFilename() { return path.getFileName().toString(); }
    @Override public String getContentType() { return "text/csv"; }
    @Override public boolean isEmpty() { return false; }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            LOG.warn("Failed to read size of {}: {}", path, exception.getMessage());
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
