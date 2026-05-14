package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.PromoBanner;
import com.haein.shoppingmall.dto.AdminPromoBannerForm;
import com.haein.shoppingmall.dto.PromoBannerResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.PromoBannerRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoBannerService {

    private final PromoBannerRepository promoBannerRepository;
    private final ItemRepository itemRepository;
    private final PromoBannerImageStorageService imageStorageService;

    public PromoBannerService(
            PromoBannerRepository promoBannerRepository,
            ItemRepository itemRepository,
            PromoBannerImageStorageService imageStorageService
    ) {
        this.promoBannerRepository = promoBannerRepository;
        this.itemRepository = itemRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<PromoBannerResponse> findBanners() {
        return promoBannerRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(PromoBannerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromoBannerResponse findBanner(Long bannerId) {
        return PromoBannerResponse.from(findBannerEntity(bannerId));
    }

    @Transactional
    public Long createBanner(AdminPromoBannerForm form) {
        validate(form);
        PromoBanner banner = new PromoBanner(
                form.getLargeText().trim(),
                form.getSmallText().trim(),
                "",
                form.getButtonLabel().trim(),
                normalizedDisplayOrder(form.getDisplayOrder()),
                findTargetItem(form.getTargetItemId())
        );
        PromoBanner savedBanner = promoBannerRepository.save(banner);
        savedBanner.updateImageUrl(imageStorageService.replaceImage(savedBanner.getId(), null, form.getImageFile()));
        return savedBanner.getId();
    }

    @Transactional
    public void updateBanner(Long bannerId, AdminPromoBannerForm form) {
        validate(form);
        PromoBanner banner = findBannerEntity(bannerId);
        banner.update(
                form.getLargeText().trim(),
                form.getSmallText().trim(),
                form.getButtonLabel().trim(),
                normalizedDisplayOrder(form.getDisplayOrder()),
                findTargetItem(form.getTargetItemId())
        );
        banner.updateImageUrl(imageStorageService.replaceImage(banner.getId(), banner.getImageUrl(), form.getImageFile()));
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        PromoBanner banner = findBannerEntity(bannerId);
        promoBannerRepository.delete(banner);
        imageStorageService.deleteImages(bannerId);
    }

    @Transactional
    public void clearTargetItem(Long itemId) {
        promoBannerRepository.clearTargetItemByItemId(itemId);
    }

    private PromoBanner findBannerEntity(Long bannerId) {
        return promoBannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다"));
    }

    private Item findTargetItem(Long itemId) {
        if (itemId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "연결할 상품을 선택해 주세요");
        }
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "연결할 상품을 찾을 수 없습니다"));
    }

    private Integer normalizedDisplayOrder(Integer displayOrder) {
        return displayOrder == null ? 0 : displayOrder;
    }

    private void validate(AdminPromoBannerForm form) {
        if (form.getLargeText() == null || form.getLargeText().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "큰 글씨를 입력해 주세요");
        }
        if (form.getSmallText() == null || form.getSmallText().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "작은 글씨를 입력해 주세요");
        }
        if (form.getButtonLabel() == null || form.getButtonLabel().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "버튼 글씨를 입력해 주세요");
        }
    }
}
