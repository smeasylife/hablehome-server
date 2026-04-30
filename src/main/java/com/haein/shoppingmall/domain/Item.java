package com.haein.shoppingmall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private Integer salePrice;

    private Integer shippingPrice;

    private String size;

    private String color;

    @Column(columnDefinition = "text")
    private String information;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCategory> itemCategories = new ArrayList<>();

    protected Item() {
    }

    public Item(String name, Integer price, Integer salePrice, Integer shippingPrice, String size, String color, String information) {
        this.name = name;
        this.price = price;
        this.salePrice = salePrice;
        this.shippingPrice = shippingPrice;
        this.size = size;
        this.color = color;
        this.information = information;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, Integer price, Integer salePrice, Integer shippingPrice, String size, String color, String information) {
        this.name = name;
        this.price = price;
        this.salePrice = salePrice;
        this.shippingPrice = shippingPrice;
        this.size = size;
        this.color = color;
        this.information = information;
    }

    public void replacePictures(List<String> pictureUrls) {
        pictures.clear();
        pictureUrls.forEach(url -> pictures.add(new ItemPicture(url, this)));
    }

    public void replaceCategories(List<Category> categories) {
        itemCategories.clear();
        categories.forEach(category -> itemCategories.add(new ItemCategory(this, category)));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public Integer getShippingPrice() {
        return shippingPrice;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getInformation() {
        return information;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ItemPicture> getPictures() {
        return pictures;
    }

    public List<ItemCategory> getItemCategories() {
        return itemCategories;
    }
}
