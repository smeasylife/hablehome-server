package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.ItemDetailResponse;
import com.haein.shoppingmall.dto.ItemListResponse;
import com.haein.shoppingmall.dto.ItemRequest;
import com.haein.shoppingmall.security.AuthMember;
import com.haein.shoppingmall.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemListResponse> findItems(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        Long memberId = authMember == null ? null : authMember.getMemberId();
        return itemService.findItems(page, memberId);
    }

    @GetMapping("/{itemId}")
    public ItemDetailResponse findItem(@PathVariable Long itemId) {
        return itemService.findItem(itemId);
    }

    @PostMapping
    public ResponseEntity<Long> createItem(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId, @Valid @RequestBody ItemRequest request) {
        itemService.updateItem(itemId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
