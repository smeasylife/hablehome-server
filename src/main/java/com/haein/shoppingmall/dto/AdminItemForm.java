package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.CategoryName;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class AdminItemForm {

    private String name;
    private Integer price;
    private Integer salePrice;
    private Integer shippingPrice = 3000;
    private String information;
    private List<String> retainedPictureUrls = new ArrayList<>();
    private List<MultipartFile> imageFiles = new ArrayList<>();
    private List<CategoryName> categories = new ArrayList<>(List.of(CategoryName.NEW));
    private List<AdminItemOptionForm> options = new ArrayList<>(List.of(new AdminItemOptionForm()));

    public static AdminItemForm from(ItemDetailResponse item) {
        AdminItemForm form = new AdminItemForm();
        form.setName(item.name());
        form.setPrice(item.price());
        form.setSalePrice(item.salePrice());
        form.setShippingPrice(item.shippingPrice());
        form.setInformation(item.information());
        form.setRetainedPictureUrls(item.itemPictures().stream()
                .map(ItemPictureResponse::url)
                .toList());
        form.setCategories(item.categories().stream()
                .map(CategoryName::valueOf)
                .toList());
        form.setOptions(item.options().stream()
                .map(option -> new AdminItemOptionForm(
                        option.color(),
                        option.size(),
                        option.stockQuantity(),
                        option.additionalPrice()
                ))
                .toList());
        return form;
    }

    public ItemRequest toItemRequest() {
        List<ItemOptionRequest> optionRequests = normalizedOptions();
        return new ItemRequest(
                name,
                price,
                salePrice,
                shippingPrice,
                summarizedSizes(optionRequests),
                summarizedColors(optionRequests),
                information,
                retainedPictureUrls == null ? List.of() : retainedPictureUrls,
                categories,
                optionRequests
        );
    }

    private List<ItemOptionRequest> normalizedOptions() {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .filter(option -> option != null)
                .map(AdminItemOptionForm::toRequest)
                .toList();
    }

    private String summarizedColors(List<ItemOptionRequest> optionRequests) {
        return optionRequests.stream()
                .map(ItemOptionRequest::color)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
    }

    private String summarizedSizes(List<ItemOptionRequest> optionRequests) {
        return optionRequests.stream()
                .map(ItemOptionRequest::size)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
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

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public List<String> getRetainedPictureUrls() {
        return retainedPictureUrls;
    }

    public void setRetainedPictureUrls(List<String> retainedPictureUrls) {
        this.retainedPictureUrls = retainedPictureUrls == null ? new ArrayList<>() : retainedPictureUrls;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles == null ? new ArrayList<>() : imageFiles;
    }

    public List<CategoryName> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryName> categories) {
        this.categories = categories == null ? new ArrayList<>() : categories;
    }

    public List<AdminItemOptionForm> getOptions() {
        return options;
    }

    public void setOptions(List<AdminItemOptionForm> options) {
        this.options = options == null || options.isEmpty()
                ? new ArrayList<>(List.of(new AdminItemOptionForm()))
                : new ArrayList<>(options);
    }
}
