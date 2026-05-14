package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Category;
import com.haein.shoppingmall.domain.CategoryName;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemOption;
import com.haein.shoppingmall.domain.ItemPicture;
import com.haein.shoppingmall.dto.ItemDetailResponse;
import com.haein.shoppingmall.dto.ItemListResponse;
import com.haein.shoppingmall.dto.ItemOptionRequest;
import com.haein.shoppingmall.dto.ItemOptionResponse;
import com.haein.shoppingmall.dto.ItemPictureResponse;
import com.haein.shoppingmall.dto.ItemRequest;
import com.haein.shoppingmall.dto.QuestionResponse;
import com.haein.shoppingmall.dto.ReviewResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CartRepository;
import com.haein.shoppingmall.repository.CategoryRepository;
import com.haein.shoppingmall.repository.ItemLikeRepository;
import com.haein.shoppingmall.repository.ItemOptionRepository;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.QuestionRepository;
import com.haein.shoppingmall.repository.ReviewRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;
    private final PromoBannerService promoBannerService;

    public ItemService(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository,
            CartRepository cartRepository,
            ItemLikeRepository itemLikeRepository,
            ItemOptionRepository itemOptionRepository,
            ReviewRepository reviewRepository,
            QuestionRepository questionRepository,
            PromoBannerService promoBannerService
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.itemLikeRepository = itemLikeRepository;
        this.itemOptionRepository = itemOptionRepository;
        this.reviewRepository = reviewRepository;
        this.questionRepository = questionRepository;
        this.promoBannerService = promoBannerService;
    }

    @Transactional(readOnly = true)
    public List<ItemListResponse> findItems(int page, Long memberId) {
        return itemRepository.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(item -> toItemListResponse(item, memberId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemListResponse> searchItems(String keyword, Long memberId) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return List.of();
        }

        return itemRepository.findByNameContainingIgnoreCase(
                        trimmedKeyword,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
                .stream()
                .map(item -> toItemListResponse(item, memberId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse findItem(Long itemId) {
        return findItem(itemId, null);
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse findItem(Long itemId, Long memberId) {
        Item item = findItemEntity(itemId);
        List<ReviewResponse> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getMember().getNickname(),
                        review.getRating(),
                        review.getProductOption(),
                        review.getPictures().stream().map(picture -> picture.getUrl()).toList(),
                        review.getContent(),
                        review.getComment() == null ? null : review.getComment().getComment(),
                        review.getCreatedAt()
                ))
                .toList();
        List<QuestionResponse> questions = questionRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getTitle(),
                        question.getContent(),
                        question.getAnswer(),
                        question.getCreatedAt()
                ))
                .toList();

        return new ItemDetailResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSalePrice(),
                item.getShippingPrice(),
                item.getSize(),
                item.getColor(),
                item.getInformation(),
                item.getPictures().stream().map(picture -> new ItemPictureResponse(picture.getUrl())).toList(),
                item.getOptions().stream().map(this::toItemOptionResponse).toList(),
                item.getItemCategories().stream()
                        .map(itemCategory -> itemCategory.getCategory().getName().name())
                        .toList(),
                memberId != null && itemLikeRepository.existsByItemIdAndMemberId(item.getId(), memberId),
                reviews,
                questions
        );
    }

    @Transactional
    public Long createItem(ItemRequest request) {
        Item item = new Item(
                request.name(),
                request.price(),
                request.salePrice(),
                request.shippingPrice(),
                request.size(),
                request.color(),
                request.information()
        );
        item.replaceOptions(toItemOptions(item, request.options()));
        item.replacePictures(request.pictureUrls() == null ? List.of() : request.pictureUrls());
        item.replaceCategories(findCategories(request.categories()));
        return itemRepository.save(item).getId();
    }

    @Transactional
    public void updateItem(Long itemId, ItemRequest request) {
        Item item = findItemEntity(itemId);
        item.update(
                request.name(),
                request.price(),
                request.salePrice(),
                request.shippingPrice(),
                request.size(),
                request.color(),
                request.information()
        );
        item.replaceOptions(toItemOptions(item, request.options()));
        item.replacePictures(request.pictureUrls() == null ? List.of() : request.pictureUrls());
        item.replaceCategories(findCategories(request.categories()));
    }

    @Transactional
    public void replaceItemPictures(Long itemId, List<String> pictureUrls) {
        Item item = findItemEntity(itemId);
        item.replacePictures(pictureUrls);
    }

    @Transactional(readOnly = true)
    public OptionStock validateOptionStock(Long itemId, String color, String size, int quantity) {
        ItemOption option = findOption(itemId, color, size);
        if (option.getStockQuantity() < quantity) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "선택한 옵션의 재고가 부족합니다");
        }
        return new OptionStock(option.getStockQuantity(), option.getStockQuantity() >= quantity);
    }

    @Transactional(readOnly = true)
    public OptionStock findOptionStock(Long itemId, String color, String size, int quantity) {
        return itemOptionRepository.findByItemIdAndColorAndSize(itemId, normalizeOption(color), normalizeOption(size))
                .map(option -> new OptionStock(option.getStockQuantity(), option.getStockQuantity() >= quantity))
                .orElse(new OptionStock(0, false));
    }

    @Transactional(readOnly = true)
    public int findOptionAdditionalPrice(Long itemId, String color, String size) {
        return findOption(itemId, color, size).getAdditionalPrice();
    }

    @Transactional(readOnly = true)
    public int effectivePrice(Item item, String color, String size) {
        return baseEffectivePrice(item) + findOptionAdditionalPrice(item.getId(), color, size);
    }

    public int baseEffectivePrice(Item item) {
        Integer salePrice = item.getSalePrice();
        if (salePrice != null && salePrice > 0) {
            return salePrice;
        }
        return item.getPrice();
    }

    @Transactional
    public void decreaseStock(Long itemId, String color, String size, int quantity) {
        ItemOption option = findOptionWithLock(itemId, color, size);
        option.decreaseStock(quantity);
    }

    @Transactional
    public void restoreStock(Long itemId, String color, String size, int quantity) {
        String normalizedColor = normalizeOption(color);
        String normalizedSize = normalizeOption(size);
        ItemOption option = itemOptionRepository.findWithLockByItemIdAndColorAndSize(itemId, normalizedColor, normalizedSize)
                .orElseGet(() -> {
                    Item item = findItemEntity(itemId);
                    return item.addOption(normalizedColor, normalizedSize, 0, 0);
                });
        option.increaseStock(quantity);
        option.getItem().refreshOptionSummary();
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item item = findItemEntity(itemId);
        cartRepository.deleteByItemId(itemId);
        itemLikeRepository.deleteByItemId(itemId);
        reviewRepository.deleteAll(reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId));
        questionRepository.deleteAll(questionRepository.findByItemIdOrderByCreatedAtDesc(itemId));
        promoBannerService.clearTargetItem(itemId);
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public Item findItemEntity(Long itemId) {
        return itemRepository.findWithPicturesById(itemId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"));
    }

    private List<Category> findCategories(List<CategoryName> categoryNames) {
        return categoryNames.stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다: " + name)))
                .toList();
    }

    private List<ItemOption> toItemOptions(Item item, List<ItemOptionRequest> options) {
        validateOptions(options);
        return options.stream()
                .map(option -> new ItemOption(
                        option.color(),
                        option.size(),
                        option.stockQuantity(),
                        normalizedAdditionalPrice(option),
                        item
                ))
                .toList();
    }

    private void validateOptions(List<ItemOptionRequest> options) {
        if (options == null || options.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "상품 옵션을 1개 이상 입력해 주세요");
        }

        Set<String> optionKeys = new LinkedHashSet<>();
        for (ItemOptionRequest option : options) {
            if (option == null || option.color() == null || option.color().isBlank()
                    || option.size() == null || option.size().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "색상과 사이즈를 입력해 주세요");
            }
            if (option.stockQuantity() == null || option.stockQuantity() < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다");
            }
            if (option.additionalPrice() != null && option.additionalPrice() < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "옵션 추가금은 0 이상이어야 합니다");
            }
            String key = normalizeOption(option.color()) + "\n" + normalizeOption(option.size());
            if (!optionKeys.add(key)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 색상과 사이즈 옵션이 중복되었습니다");
            }
        }
    }

    private int normalizedAdditionalPrice(ItemOptionRequest option) {
        return option.additionalPrice() == null ? 0 : option.additionalPrice();
    }

    private ItemOption findOption(Long itemId, String color, String size) {
        return itemOptionRepository.findByItemIdAndColorAndSize(itemId, normalizeOption(color), normalizeOption(size))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "판매 중인 상품 옵션이 아닙니다"));
    }

    private ItemOption findOptionWithLock(Long itemId, String color, String size) {
        return itemOptionRepository.findWithLockByItemIdAndColorAndSize(itemId, normalizeOption(color), normalizeOption(size))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "판매 중인 상품 옵션이 아닙니다"));
    }

    private String normalizeOption(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "상품 옵션을 선택해 주세요");
        }
        return value.trim();
    }

    private String firstPictureUrl(Item item) {
        return item.getPictures().stream()
                .findFirst()
                .map(ItemPicture::getUrl)
                .orElse("");
    }

    private ItemListResponse toItemListResponse(Item item, Long memberId) {
        return new ItemListResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSalePrice(),
                item.getColor(),
                firstPictureUrl(item),
                memberId != null && itemLikeRepository.existsByItemIdAndMemberId(item.getId(), memberId),
                item.getItemCategories().stream()
                        .map(itemCategory -> itemCategory.getCategory().getName().name())
                        .toList()
        );
    }

    private ItemOptionResponse toItemOptionResponse(ItemOption option) {
        return new ItemOptionResponse(
                option.getId(),
                option.getColor(),
                option.getSize(),
                option.getStockQuantity(),
                option.getAdditionalPrice(),
                option.getStockQuantity() <= 0
        );
    }

    public record OptionStock(Integer stockQuantity, boolean available) {
    }
}
