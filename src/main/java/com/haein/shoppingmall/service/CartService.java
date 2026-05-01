package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Cart;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemPicture;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.dto.CartRequest;
import com.haein.shoppingmall.dto.CartItemResponse;
import com.haein.shoppingmall.repository.CartRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ItemService itemService;
    private final MemberService memberService;

    public CartService(CartRepository cartRepository, ItemService itemService, MemberService memberService) {
        this.cartRepository = cartRepository;
        this.itemService = itemService;
        this.memberService = memberService;
    }

    @Transactional
    public void addCart(Long itemId, CartRequest request, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        Item item = itemService.findItemEntity(itemId);
        String color = optionOrDefault(request == null ? null : request.color(), item.getColor());
        String size = optionOrDefault(request == null ? null : request.size(), item.getSize());
        int quantity = request == null || request.quantity() == null ? 1 : request.quantity();
        cartRepository.findByItemIdAndMemberIdAndColorAndSize(itemId, member.getId(), color, size)
                .ifPresentOrElse(
                        cart -> cart.increaseQuantity(quantity),
                        () -> cartRepository.save(new Cart(item, member, color, size, quantity))
                );
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> findCartItems(Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        return cartRepository.findByMemberIdOrderByIdDesc(member.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void removeSelectedCartItems(List<Long> cartIds, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        cartRepository.deleteByIdInAndMemberId(cartIds, member.getId());
    }

    private CartItemResponse toResponse(Cart cart) {
        Item item = cart.getItem();
        return new CartItemResponse(
                cart.getId(),
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSalePrice(),
                cart.getColor(),
                cart.getSize(),
                cart.getQuantity(),
                firstPictureUrl(item)
        );
    }

    private String optionOrDefault(String option, String defaultValue) {
        if (option == null || option.isBlank()) {
            return defaultValue;
        }
        return option;
    }

    private String firstPictureUrl(Item item) {
        return item.getPictures().stream()
                .findFirst()
                .map(ItemPicture::getUrl)
                .orElse("");
    }
}
