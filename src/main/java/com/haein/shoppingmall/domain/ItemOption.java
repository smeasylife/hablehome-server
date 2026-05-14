package com.haein.shoppingmall.domain;

import com.haein.shoppingmall.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.http.HttpStatus;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"item_id", "color", "size"})
})
public class ItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer additionalPrice = 0;

    protected ItemOption() {
    }

    public ItemOption(String color, String size, Integer stockQuantity, Item item) {
        this(color, size, stockQuantity, 0, item);
    }

    public ItemOption(String color, String size, Integer stockQuantity, Integer additionalPrice, Item item) {
        if (color == null || color.isBlank() || size == null || size.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "색상과 사이즈를 입력해 주세요");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다");
        }
        if (additionalPrice == null || additionalPrice < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "옵션 추가금은 0 이상이어야 합니다");
        }
        this.color = color.trim();
        this.size = size.trim();
        this.stockQuantity = stockQuantity;
        this.additionalPrice = additionalPrice;
        this.item = item;
    }

    void assignItem(Item item) {
        this.item = item;
    }

    public void decreaseStock(int quantity) {
        if (quantity < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "수량은 1개 이상이어야 합니다");
        }
        if (stockQuantity < quantity) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "선택한 옵션의 재고가 부족합니다");
        }
        stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity < 1) {
            return;
        }
        stockQuantity += quantity;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Integer getAdditionalPrice() {
        return additionalPrice == null ? 0 : additionalPrice;
    }
}
