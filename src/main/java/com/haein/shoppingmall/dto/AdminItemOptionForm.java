package com.haein.shoppingmall.dto;

public class AdminItemOptionForm {

    private String color;
    private String size;
    private Integer stockQuantity;
    private Integer additionalPrice = 0;

    public AdminItemOptionForm() {
    }

    public AdminItemOptionForm(String color, String size, Integer stockQuantity, Integer additionalPrice) {
        this.color = color;
        this.size = size;
        this.stockQuantity = stockQuantity;
        this.additionalPrice = additionalPrice;
    }

    public ItemOptionRequest toRequest() {
        return new ItemOptionRequest(color, size, stockQuantity, additionalPrice);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(Integer additionalPrice) {
        this.additionalPrice = additionalPrice;
    }
}
