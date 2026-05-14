package com.haein.shoppingmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class PromoBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String largeText;

    @Column(columnDefinition = "text")
    private String smallText;

    private String imageUrl;

    private String buttonLabel;

    private Integer displayOrder;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_item_id")
    private Item targetItem;

    protected PromoBanner() {
    }

    public PromoBanner(
            String largeText,
            String smallText,
            String imageUrl,
            String buttonLabel,
            Integer displayOrder,
            Item targetItem
    ) {
        this.largeText = largeText;
        this.smallText = smallText;
        this.imageUrl = imageUrl;
        this.buttonLabel = buttonLabel;
        this.displayOrder = displayOrder;
        this.targetItem = targetItem;
        this.createdAt = LocalDateTime.now();
    }

    public void update(
            String largeText,
            String smallText,
            String buttonLabel,
            Integer displayOrder,
            Item targetItem
    ) {
        this.largeText = largeText;
        this.smallText = smallText;
        this.buttonLabel = buttonLabel;
        this.displayOrder = displayOrder;
        this.targetItem = targetItem;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getLargeText() {
        return largeText;
    }

    public String getSmallText() {
        return smallText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Item getTargetItem() {
        return targetItem;
    }
}
