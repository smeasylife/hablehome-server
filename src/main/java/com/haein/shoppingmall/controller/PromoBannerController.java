package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.PromoBannerResponse;
import com.haein.shoppingmall.service.PromoBannerService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promo-banners")
public class PromoBannerController {

    private final PromoBannerService promoBannerService;

    public PromoBannerController(PromoBannerService promoBannerService) {
        this.promoBannerService = promoBannerService;
    }

    @GetMapping
    public List<PromoBannerResponse> findBanners() {
        return promoBannerService.findBanners();
    }
}
