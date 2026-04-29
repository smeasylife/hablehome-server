package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Cart;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemPicture;
import com.haein.shoppingmall.domain.Member;
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
    public void addCart(Long itemId, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        if (cartRepository.existsByItemIdAndMemberId(itemId, member.getId())) {
            return;
        }
        Item item = itemService.findItemEntity(itemId);
        cartRepository.save(new Cart(item, member));
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
                item.getColor(),
                item.getSize(),
                firstPictureUrl(item)
        );
    }

    private String firstPictureUrl(Item item) {
        return item.getPictures().stream()
                .findFirst()
                .map(ItemPicture::getUrl)
                .orElse("");
    }
}
