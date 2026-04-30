package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.CategoryName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminItemForm {

    private String name;
    private Integer price;
    private Integer salePrice;
    private Integer shippingPrice = 3000;
    private String size;
    private String color;
    private String information;
    private String pictureUrls;
    private List<CategoryName> categories = new ArrayList<>(List.of(CategoryName.NEW));

    public static AdminItemForm from(ItemDetailResponse item) {
        AdminItemForm form = new AdminItemForm();
        form.setName(item.name());
        form.setPrice(item.price());
        form.setSalePrice(item.salePrice());
        form.setShippingPrice(item.shippingPrice());
        form.setSize(item.size());
        form.setColor(item.color());
        form.setInformation(item.information());
        form.setPictureUrls(String.join("\n", item.itemPictures().stream()
                .map(ItemPictureResponse::url)
                .toList()));
        form.setCategories(item.categories().stream()
                .map(CategoryName::valueOf)
                .toList());
        return form;
    }

    public ItemRequest toItemRequest() {
        return new ItemRequest(
                name,
                price,
                salePrice,
                shippingPrice,
                size,
                color,
                information,
                splitPictureUrls(),
                categories
        );
    }

    private List<String> splitPictureUrls() {
        if (pictureUrls == null || pictureUrls.isBlank()) {
            return List.of();
        }
        return Arrays.stream(pictureUrls.split("\\R"))
                .map(String::trim)
                .filter(url -> !url.isBlank())
                .toList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(Integer shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(String pictureUrls) {
        this.pictureUrls = pictureUrls;
    }

    public List<CategoryName> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryName> categories) {
        this.categories = categories == null ? new ArrayList<>() : categories;
    }
}
