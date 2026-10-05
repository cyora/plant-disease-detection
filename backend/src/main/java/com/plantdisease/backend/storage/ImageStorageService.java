package com.plantdisease.backend.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Stores uploaded photos and heatmaps on disk, in one folder per month.
 * The database only keeps the relative path (e.g. "2026-10/3f2a...c9.jpg").
 */
@Service
public class ImageStorageService {

    private final Path root;

    public ImageStorageService(@Value("${app.storage.dir}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Saves the bytes under a random name and returns the relative path. */
    public String save(byte[] bytes, String extension) {
        String relative = YearMonth.now() + "/" + UUID.randomUUID() + extension;
        Path target = resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save image " + relative, e);
        }
        return relative;
    }

    public byte[] load(String relativePath) {
        try {
            return Files.readAllBytes(resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read image " + relativePath, e);
        }
    }

    public void delete(String relativePath) {
        if (relativePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete image " + relativePath, e);
        }
    }

    /** Resolves a relative path and refuses anything outside the storage folder. */
    private Path resolve(String relativePath) {
        Path path = root.resolve(relativePath).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        return path;
    }
}
