package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.PromoBanner;

public record PromoBannerResponse(
        Long id,
        String largeText,
        String smallText,
        String imageUrl,
        String buttonLabel,
        Long itemId,
        String itemName,
        String linkUrl,
        Integer displayOrder
) {

    public static PromoBannerResponse from(PromoBanner banner) {
        Item item = banner.getTargetItem();
        Long itemId = item == null ? null : item.getId();
        return new PromoBannerResponse(
                banner.getId(),
                banner.getLargeText(),
                banner.getSmallText(),
                banner.getImageUrl(),
                banner.getButtonLabel(),
                itemId,
                item == null ? "연결 상품 없음" : item.getName(),
                itemId == null ? null : "/items/" + itemId,
                banner.getDisplayOrder()
        );
    }
}
