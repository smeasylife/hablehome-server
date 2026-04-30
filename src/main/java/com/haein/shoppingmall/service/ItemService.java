package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Category;
import com.haein.shoppingmall.domain.CategoryName;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemPicture;
import com.haein.shoppingmall.dto.ItemDetailResponse;
import com.haein.shoppingmall.dto.ItemListResponse;
import com.haein.shoppingmall.dto.ItemPictureResponse;
import com.haein.shoppingmall.dto.ItemRequest;
import com.haein.shoppingmall.dto.QuestionResponse;
import com.haein.shoppingmall.dto.ReviewResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CartRepository;
import com.haein.shoppingmall.repository.CategoryRepository;
import com.haein.shoppingmall.repository.ItemLikeRepository;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.QuestionRepository;
import com.haein.shoppingmall.repository.ReviewRepository;
import java.util.List;
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
    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;

    public ItemService(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository,
            CartRepository cartRepository,
            ItemLikeRepository itemLikeRepository,
            ReviewRepository reviewRepository,
            QuestionRepository questionRepository
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.itemLikeRepository = itemLikeRepository;
        this.reviewRepository = reviewRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemListResponse> findItems(int page, Long memberId) {
        return itemRepository.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(item -> new ItemListResponse(
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getSalePrice(),
                        item.getColor(),
                        firstPictureUrl(item),
                        memberId != null && itemLikeRepository.existsByItemIdAndMemberId(item.getId(), memberId)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse findItem(Long itemId) {
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
                item.getItemCategories().stream()
                        .map(itemCategory -> itemCategory.getCategory().getName().name())
                        .toList(),
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
        item.replacePictures(request.pictureUrls());
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
        item.replacePictures(request.pictureUrls());
        item.replaceCategories(findCategories(request.categories()));
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item item = findItemEntity(itemId);
        cartRepository.deleteByItemId(itemId);
        itemLikeRepository.deleteByItemId(itemId);
        reviewRepository.deleteAll(reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId));
        questionRepository.deleteAll(questionRepository.findByItemIdOrderByCreatedAtDesc(itemId));
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

    private String firstPictureUrl(Item item) {
        return item.getPictures().stream()
                .findFirst()
                .map(ItemPicture::getUrl)
                .orElse("");
    }
}
