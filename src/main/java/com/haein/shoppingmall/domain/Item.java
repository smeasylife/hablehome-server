package com.haein.shoppingmall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @OrderBy("displayOrder ASC, id ASC")
    private List<ItemPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCategory> itemCategories = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOption> options = new ArrayList<>();

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
        for (int index = 0; index < pictureUrls.size(); index++) {
            pictures.add(new ItemPicture(pictureUrls.get(index), this, index));
        }
    }

    public void replaceCategories(List<Category> categories) {
        itemCategories.clear();
        categories.forEach(category -> itemCategories.add(new ItemCategory(this, category)));
    }

    public void replaceOptions(List<ItemOption> itemOptions) {
        Map<String, ItemOption> existingOptions = new LinkedHashMap<>();
        options.forEach(option -> existingOptions.put(optionKey(option.getColor(), option.getSize()), option));
        Map<String, ItemOption> requestedOptions = new LinkedHashMap<>();
        itemOptions.forEach(option -> requestedOptions.put(optionKey(option.getColor(), option.getSize()), option));

        options.removeIf(option -> !requestedOptions.containsKey(optionKey(option.getColor(), option.getSize())));
        requestedOptions.forEach((key, option) -> {
            ItemOption existingOption = existingOptions.get(key);
            if (existingOption != null) {
                existingOption.update(
                        option.getColor(),
                        option.getSize(),
                        option.getStockQuantity(),
                        option.getAdditionalPrice()
                );
                return;
            }
            option.assignItem(this);
            options.add(option);
        });
        refreshOptionSummary();
    }

    private String optionKey(String color, String size) {
        return color.trim() + "\n" + size.trim();
    }

    public ItemOption addOption(String color, String size, Integer stockQuantity) {
        return addOption(color, size, stockQuantity, 0);
    }

    public ItemOption addOption(String color, String size, Integer stockQuantity, Integer additionalPrice) {
        ItemOption option = new ItemOption(color, size, stockQuantity, additionalPrice, this);
        options.add(option);
        refreshOptionSummary();
        return option;
    }

    public void refreshOptionSummary() {
        this.color = options.stream()
                .map(ItemOption::getColor)
                .distinct()
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
        this.size = options.stream()
                .map(ItemOption::getSize)
                .distinct()
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
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

    public List<ItemOption> getOptions() {
        return options;
    }
}
