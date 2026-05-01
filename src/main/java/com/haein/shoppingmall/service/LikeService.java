package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemLike;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.repository.ItemLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final ItemLikeRepository itemLikeRepository;
    private final ItemService itemService;
    private final MemberService memberService;

    public LikeService(ItemLikeRepository itemLikeRepository, ItemService itemService, MemberService memberService) {
        this.itemLikeRepository = itemLikeRepository;
        this.itemService = itemService;
        this.memberService = memberService;
    }

    @Transactional
    public void like(Long itemId, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        if (itemLikeRepository.existsByItemIdAndMemberId(itemId, member.getId())) {
            return;
        }
        Item item = itemService.findItemEntity(itemId);
        itemLikeRepository.save(new ItemLike(item, member));
    }

    @Transactional
    public void unlike(Long itemId, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        itemLikeRepository.deleteByItemIdAndMemberId(itemId, member.getId());
    }
}
