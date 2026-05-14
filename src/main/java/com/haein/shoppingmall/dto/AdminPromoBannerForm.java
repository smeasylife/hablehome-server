package com.haein.shoppingmall.dto;

import org.springframework.web.multipart.MultipartFile;

public class AdminPromoBannerForm {

    private String largeText;
    private String smallText;
    private MultipartFile imageFile;
    private String retainedImageUrl;
    private String buttonLabel = "상품 보기";
    private Long targetItemId;
    private Integer displayOrder = 0;

    public static AdminPromoBannerForm from(PromoBannerResponse banner) {
        AdminPromoBannerForm form = new AdminPromoBannerForm();
        form.setLargeText(banner.largeText());
        form.setSmallText(banner.smallText());
        form.setRetainedImageUrl(banner.imageUrl());
        form.setButtonLabel(banner.buttonLabel());
        form.setTargetItemId(banner.itemId());
        form.setDisplayOrder(banner.displayOrder());
        return form;
    }

    public String getLargeText() {
        return largeText;
    }

    public void setLargeText(String largeText) {
        this.largeText = largeText;
    }

    public String getSmallText() {
        return smallText;
    }

    public void setSmallText(String smallText) {
        this.smallText = smallText;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public String getRetainedImageUrl() {
        return retainedImageUrl;
    }

    public void setRetainedImageUrl(String retainedImageUrl) {
        this.retainedImageUrl = retainedImageUrl;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public Long getTargetItemId() {
        return targetItemId;
    }

    public void setTargetItemId(Long targetItemId) {
        this.targetItemId = targetItemId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
