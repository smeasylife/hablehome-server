package com.haein.shoppingmall.service;

import com.haein.shoppingmall.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PromoBannerImageStorageService {

    private final Path bannerImageRoot;

    public PromoBannerImageStorageService(@Value("${app.images.storage-dir:images}") String storageDir) {
        this.bannerImageRoot = Path.of(storageDir).toAbsolutePath().normalize().resolve("banners");
    }

    public String replaceImage(Long bannerId, String currentImageUrl, MultipartFile upload) {
        if (upload == null || upload.isEmpty()) {
            if (currentImageUrl == null || currentImageUrl.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "배너 이미지를 등록해 주세요");
            }
            return currentImageUrl;
        }

        String extension = extension(upload.getOriginalFilename());
        Path bannerDir = bannerImageRoot.resolve(String.valueOf(bannerId));
        Path tempDir = bannerImageRoot.resolve(bannerId + "-tmp-" + System.nanoTime());

        try {
            Files.createDirectories(tempDir);
            String filename = "001." + extension;
            Files.copy(upload.getInputStream(), tempDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            deleteDirectory(bannerDir);
            Files.createDirectories(bannerImageRoot);
            Files.move(tempDir, bannerDir, StandardCopyOption.REPLACE_EXISTING);
            return "/images/banners/" + bannerId + "/" + filename;
        } catch (IOException exception) {
            deleteDirectory(tempDir);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배너 이미지를 저장하지 못했습니다", exception);
        }
    }

    public void deleteImages(Long bannerId) {
        deleteDirectory(bannerImageRoot.resolve(String.valueOf(bannerId)));
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지 파일 확장자를 확인해 주세요");
        }
        String extension = filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg", "png", "webp", "gif" -> extension;
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "jpg, png, webp, gif 이미지만 업로드할 수 있습니다");
        };
    }

    private void deleteDirectory(Path directory) {
        try {
            if (!Files.exists(directory)) {
                return;
            }
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                });
            }
        } catch (IOException | IllegalStateException ignored) {
        }
    }
}
