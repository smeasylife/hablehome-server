package com.haein.shoppingmall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ItemPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    protected ItemPicture() {
    }

    public ItemPicture(String url, Item item) {
        this(url, item, 0);
    }

    public ItemPicture(String url, Item item, Integer displayOrder) {
        this.url = url;
        this.item = item;
        this.displayOrder = displayOrder;
    }

    public String getUrl() {
        return url;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
