package com.haein.shoppingmall.service;

import com.haein.shoppingmall.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ItemImageStorageService {

    private final Path itemImageRoot;

    public ItemImageStorageService(@Value("${app.images.storage-dir:images}") String storageDir) {
        this.itemImageRoot = Path.of(storageDir).toAbsolutePath().normalize().resolve("items");
    }

    public List<String> replaceImages(Long itemId, List<String> retainedUrls, List<MultipartFile> uploads) {
        List<String> urls = new ArrayList<>();
        Path itemDir = itemImageRoot.resolve(String.valueOf(itemId));
        Path tempDir = itemImageRoot.resolve(itemId + "-tmp-" + System.nanoTime());

        try {
            Files.createDirectories(tempDir);
            int order = 1;
            for (String retainedUrl : retainedUrls == null ? List.<String>of() : retainedUrls) {
                if (retainedUrl == null || retainedUrl.isBlank()) {
                    continue;
                }
                if (!retainedUrl.startsWith("/images/items/" + itemId + "/")) {
                    urls.add(retainedUrl);
                    continue;
                }
                Path source = itemDir.resolve(Path.of(retainedUrl).getFileName().toString()).normalize();
                if (!source.startsWith(itemDir) || !Files.exists(source)) {
                    continue;
                }
                String filename = orderedFilename(order++, extension(source.getFileName().toString()));
                Files.copy(source, tempDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add("/images/items/" + itemId + "/" + filename);
            }

            for (MultipartFile upload : uploads == null ? List.<MultipartFile>of() : uploads) {
                if (upload == null || upload.isEmpty()) {
                    continue;
                }
                String extension = extension(upload.getOriginalFilename());
                if (extension.isBlank()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지 파일 확장자를 확인해 주세요");
                }
                String filename = orderedFilename(order++, extension);
                Files.copy(upload.getInputStream(), tempDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add("/images/items/" + itemId + "/" + filename);
            }

            if (urls.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "상품 이미지를 1개 이상 등록해 주세요");
            }

            deleteDirectory(itemDir);
            Files.createDirectories(itemImageRoot);
            Files.move(tempDir, itemDir, StandardCopyOption.REPLACE_EXISTING);
            return urls;
        } catch (BusinessException exception) {
            deleteDirectory(tempDir);
            throw exception;
        } catch (IOException exception) {
            deleteDirectory(tempDir);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "상품 이미지를 저장하지 못했습니다", exception);
        }
    }

    private String orderedFilename(int order, String extension) {
        return String.format("%03d.%s", order, extension.toLowerCase(Locale.ROOT));
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
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
